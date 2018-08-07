package magnolia.bson.derivation.reader

import magnolia.bson.BSONReadDerivation
import magnolia.{CaseClass, Magnolia, SealedTrait, debug}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONReader, BSONValue, VariantBSONReader}

import scala.language.experimental.macros

object semiauto {

//  type B <: BSONValue
  private type Typeclass[T] = BSONReadDerivation.Typeclass[T]
//  
//  val d = new BSONReadDerivation[B] {}

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T]  =
    BSONReadDerivation.combine(caseClass)

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] =
    BSONReadDerivation.dispatch(sealedTrait)

  def deriveMagnoliaReader[T]: Typeclass[T] = macro Magnolia.gen[T]
}

//object semiauto extends semiauto
