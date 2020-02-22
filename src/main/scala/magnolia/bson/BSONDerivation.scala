package magnolia.bson

import magnolia._
import reactivemongo.bson._

import scala.util.{Failure, Success}

object BSONReadDerivation {

  type Typeclass[T] = BSONReader[_ <: BSONValue, T]

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = {
    BSONDocumentReader(doc =>
      caseClass.construct { p =>
        doc.getAsUnflattenedTry(p.label)(p.typeclass) match {
          case Success(Some(v)) => v
          case Success(None) => p.default.get // TODO better ex
          case Failure(ex) => throw ex
        }
      }
    )
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
    BSONDocumentReader(doc => {
      val className = doc.getAs[String]("className").getOrElse(throw new IllegalStateException("'className' is required for sealed traits"))
      val subtype = sealedTrait.subtypes.find(_.typeName.full == className).get
      subtype.typeclass.asInstanceOf[BSONReader[BSONValue, T]].read(doc)
    })
  }

}

object BSONWriteDerivation {

  type Typeclass[T] = BSONWriter[T, _ <: BSONValue]

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = {
    BSONDocumentWriter(t => BSONDocument(caseClass.parameters.map(p => (p.label, p.typeclass.write(p.dereference(t))))))
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
    BSONDocumentWriter(t => sealedTrait.dispatch(t) { subType =>
        subType.typeclass.writeTry(subType.cast(t)) match {
          case Success(doc@BSONDocument(_)) => (doc ++ ("className" -> subType.typeName.full))
          case Success(v) => throw new Exception("should write a document...")
          case Failure(ex) => throw ex
        }
      })
  }
}

object test {
  val a0: BSONReader[BSONString, String] = DefaultBSONHandlers.BSONStringHandler
  val a1: BSONReader[_, String] = a0

//  val b: BSONReadDerivation.Typeclass[Seq[String]] = DefaultBSONHandlers.bsonArrayToCollectionReader[Seq, String]

}
