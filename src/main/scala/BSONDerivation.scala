import reactivemongo.bson.{BSONDocument, BSONHandler, BSONValue}

import scala.util.{Failure, Success}

object BSONDerivation {

  import magnolia._
  import scala.language.experimental.macros

  type Typeclass[T] = BSONHandler[_ <: BSONValue, T]

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = {

    def write(t: T): BSONDocument =
      BSONDocument(caseClass.parameters.map(p => (p.label, p.typeclass.write(p.dereference(t)))))

    def read(bsonValue: BSONValue): T = bsonValue match {
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

    BSONHandler(read, write)
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
    def write(t: T): BSONValue = {
      sealedTrait.dispatch(t) { subType =>
        subType.typeclass.writeTry(subType.cast(t)) match {
          case Success(doc@BSONDocument(_)) => doc ++ ("className" -> subType.typeName.full)
          case Success(v) => v
          case Failure(ex) => throw ex
        }

      }
    }

    def read(bsonValue: BSONValue): T = bsonValue match {
      case doc: BSONDocument =>
        val className = doc.getAs[String]("className").getOrElse(throw new IllegalStateException("'className' is required for sealed traits"))
        val subtype = sealedTrait.subtypes.find(_.typeName.full == className).get
        subtype.typeclass.asInstanceOf[BSONHandler[BSONValue, T]].read(doc)
      case _ => throw new IllegalStateException("we only handle-case classes")
    }

    BSONHandler(read, write)
  }

  implicit def gen[T]: BSONHandler[_ <: BSONValue, T] = macro Magnolia.gen[T]
}
