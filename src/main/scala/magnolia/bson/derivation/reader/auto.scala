package magnolia.bson.derivation.reader

import magnolia.bson.BSONReadDerivation
import magnolia.{CaseClass, Magnolia, SealedTrait}
import reactivemongo.bson.{BSONReader, BSONValue}

import scala.language.experimental.macros

object auto {

//  type Typeclass[T] = BSONReader[_ <: BSONValue, T]
//
//  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] =
//    BSONReadDerivation.combine(caseClass)
//
//  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] =
//    BSONReadDerivation.dispatch(sealedTrait)
//
//  implicit def magnoliaReader[T]: Typeclass[T] = macro Magnolia.gen[T]
}
