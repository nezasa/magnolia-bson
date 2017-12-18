import reactivemongo.bson.{BSONDocument, BSONHandler, BSONValue}

import scala.util.{Failure, Success}

object BSONDerivation {

  import magnolia._
  import scala.language.experimental.macros

  type BSONValueHandler[T] = BSONHandler[_ <: BSONValue, T]

  type Typeclass[T] = BSONValueHandler[T]

  def combine[T](caseClass: CaseClass[BSONValueHandler, T]): BSONValueHandler[T] = {

    def write(t: T): BSONDocument =
      BSONDocument(caseClass.parameters.map(p => (p.label, p.typeclass.write(p.dereference(t)))))

    def read(bsonValue: BSONValue): T = bsonValue match {
      case doc: BSONDocument =>
        caseClass.construct { p =>
          doc.getUnflattenedTry(p.label) match {
            case Success(Some(v)) => p.typeclass.asInstanceOf[BSONHandler[BSONValue, T]].read(v)
            case Success(None) => p.default.get // TODO better ex
            case Failure(ex) => throw ex
          }
        }
      case _ => throw new IllegalStateException("we only handle-case classes")
    }

    BSONHandler(read, write)
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
    def write(t: T): BSONDocument = {
      sealedTrait.dispatch(t) { subType =>
        // any case class should serialize to a BSONDocument, so cast should be okay
        val doc: BSONDocument = subType.typeclass.write(subType.cast(t)).asInstanceOf[BSONDocument]
        doc ++ ("className" -> subType.label)
      }
    }

    def read(bsonValue: BSONValue): T = bsonValue match {
      case doc: BSONDocument =>
        val className = doc.getAs[String]("className").getOrElse(throw new IllegalStateException("'className' is required for sealed traits"))
        val subtype = sealedTrait.subtypes.find(_.label == className).get
        subtype.typeclass.asInstanceOf[BSONHandler[BSONValue, T]].read(doc)
      case _ => throw new IllegalStateException("we only handle-case classes")
    }

    BSONHandler(read, write)
  }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}
