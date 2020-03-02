package magnolia.bson

import magnolia._
import reactivemongo.bson._
import reactivemongo.bson.exceptions.DocumentKeyNotFound

import scala.util.{Failure, Success, Try}

object BSONReadDerivation {

  type Typeclass[T] = BSONReader[_ <: BSONValue, T]

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = {
    BSONDocumentReader(doc =>
      caseClass.construct { p =>
        doc.getAsTry(p.label)(p.typeclass) match {
          case Success(v) => v
          case Failure(ex) =>
            if(ex.isInstanceOf[DocumentKeyNotFound] && p.typeclass.isInstanceOf[OptionReader[_, _]]) None else throw ex
        }
      }
    )
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
    BSONDocumentReader(doc => {
      val className = doc.getAs[String]("className").getOrElse(throw new IllegalStateException("'className' is required for sealed traits"))
      val subtype = sealedTrait.subtypes.find(_.typeName.full == className).get
      subtype.typeclass.asInstanceOf[BSONDocumentReader[T]].read(doc)
    })
  }

  //another workaround
  class OptionReader[T, B <: BSONValue](private val reader: BSONReader[B, T]) extends BSONReader[B, Option[T]] {
    override def read(bson: B): Option[T] = bson.asInstanceOf[BSONValue] match {
      case BSONNull => None
      case _ => Some(reader.read(bson))
    }
  }
  def optionReader[T, B <: BSONValue](implicit reader: BSONReader[B, T]): BSONReader[B, Option[T]] = new OptionReader(reader)

}

object BSONWriteDerivation {

  type Typeclass[T] = BSONWriter[T, _ <: BSONValue]

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = {
    BSONDocumentWriter(t => BSONDocument(caseClass.parameters.flatMap{ p =>
      val value = p.dereference(t)
      if (value == None) None
      else {
        Some((p.label, p.typeclass.write(value)))
      }
    }))
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

  //another workaround
  def optionWriter[T, B <: BSONValue](implicit writer: BSONWriter[T, B]): BSONWriter[Option[T], B] =
    BSONWriter(t => t.fold[B](BSONNull.asInstanceOf[B])(writer.write))
}
