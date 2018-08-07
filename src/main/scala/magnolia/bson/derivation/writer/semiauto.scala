package magnolia.bson.derivation.writer

import magnolia.bson.BSONWriteDerivation
import magnolia.{CaseClass, Magnolia, SealedTrait}
import reactivemongo.bson.{BSONDocument, BSONValue, BSONWriter}

import scala.language.experimental.macros

object semiauto {

//  type B <: BSONValue
  private type Typeclass[T] = BSONWriteDerivation.Typeclass[T]
//
//  val d = new BSONWriteDerivation[B] {}

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] =
    BSONWriteDerivation.combine(caseClass)

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] =
    BSONWriteDerivation.dispatch(sealedTrait)

  def deriveMagnoliaWriter[T]: Typeclass[T] = macro Magnolia.gen[T]
}
