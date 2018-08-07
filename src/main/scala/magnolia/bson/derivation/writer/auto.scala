package magnolia.bson.derivation.writer

import magnolia.bson.BSONWriteDerivation
import magnolia.{CaseClass, Magnolia, SealedTrait}
import reactivemongo.bson.{BSONValue, BSONWriter}

import scala.language.experimental.macros

object auto {
//
//  type Typeclass[T] = BSONWriter[T, _ <: BSONValue]
//
//  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] =
//    BSONWriteDerivation.combine(caseClass)
//
//  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] =
//    BSONWriteDerivation.dispatch(sealedTrait)
//
//  implicit def magnoliaWriter[T]: Typeclass[T] = macro Magnolia.gen[T]
}
