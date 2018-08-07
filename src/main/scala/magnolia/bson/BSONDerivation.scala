package magnolia.bson

import magnolia._
import reactivemongo.bson
import reactivemongo.bson._

import scala.util.{Failure, Success}

//case class Exported[T](instance: T) extends AnyVal
//object Exported {
//  implicit final def a[A](implicit reader: BSONReader[_ <: BSONValue, A]): Exported[BSONReader[_ <: BSONValue, A]] = Exported(reader)
//  implicit final def b[A](implicit reader: BSONWriter[A, _ <: BSONValue]): Exported[BSONWriter[A, _ <: BSONValue]] = Exported(reader)
//}

object BSONReadDerivation {

  type Typeclass[T] = BSONReader[_ <: BSONValue, T]

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = {
    new BSONReader[BSONValue, T] {
      override def read(bson: BSONValue): T = bson match {
        case doc: BSONDocument =>
          caseClass.construct { p =>
            doc.getAsUnflattenedTry(p.label)(p.typeclass) match {
              case Success(Some(v)) => v
              case Success(None) => p.default.get // TODO better ex
              case Failure(ex) => throw ex
            }
          }
        case _ => throw new IllegalStateException("we only handle-case classes")
      }
    }
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
    new BSONReader[BSONValue, T] {
      override def read(bson: BSONValue): T = {
        bson match {
          case doc: BSONDocument =>
            val className = doc.getAs[String]("className").getOrElse(throw new IllegalStateException("'className' is required for sealed traits"))
            val subtype = sealedTrait.subtypes.find(_.typeName.full == className).get
            subtype.typeclass.asInstanceOf[BSONReader[BSONValue, T]].read(doc)
          case _ => throw new IllegalStateException("we only handle-case classes")
        }
      }
    }
  }

}

object BSONWriteDerivation {

  type Typeclass[T] = BSONWriter[T, _ <: BSONValue]

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = {
    new BSONWriter[T, BSONValue]() {
      override def write(t: T): BSONDocument = BSONDocument(caseClass.parameters.map(p => (p.label, p.typeclass.write(p.dereference(t)))))
    }
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
    new BSONWriter[T, BSONValue]() {
      override def write(t: T): BSONValue = sealedTrait.dispatch(t) { subType =>
        subType.typeclass.writeTry(subType.cast(t)) match {
          case Success(doc@BSONDocument(_)) => (doc ++ ("className" -> subType.typeName.full))
          case Success(v) => v
          case Failure(ex) => throw ex
        }
      }
    }
  }
}

object test {
  import reactivemongo.bson.DefaultBSONHandlers._
  val a0: BSONReader[BSONString, String] = DefaultBSONHandlers.BSONStringHandler
  val a1: BSONReader[_, String] = a0

//  val b: BSONReadDerivation.Typeclass[Seq[String]] = DefaultBSONHandlers.bsonArrayToCollectionReader[Seq, String]

}
