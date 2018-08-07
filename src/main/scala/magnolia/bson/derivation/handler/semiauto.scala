package magnolia.bson.derivation.handler

import magnolia.bson.{BSONReadDerivation, BSONWriteDerivation}
import magnolia.{CaseClass, Magnolia, SealedTrait}
import reactivemongo.bson.{BSONDocumentHandler, BSONHandler, BSONReader, BSONValue, BSONWriter}

import scala.language.experimental.macros

object semiauto {

  type Typeclass[T] = BSONHandler[_ <: BSONValue, T]

  val readDerivation = BSONReadDerivation
  val writeDerivation = BSONWriteDerivation

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = BSONHandler.provided[BSONValue, T](
    writeDerivation.combine(caseClass.asInstanceOf[CaseClass[writeDerivation.Typeclass,T]]).asInstanceOf[BSONWriter[T, BSONValue]],
    readDerivation.combine(caseClass.asInstanceOf[CaseClass[readDerivation.Typeclass,T]]).asInstanceOf[BSONReader[BSONValue, T]]
  )

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = BSONHandler.provided[BSONValue, T](
    writeDerivation.dispatch(sealedTrait.asInstanceOf[SealedTrait[writeDerivation.Typeclass,T]]).asInstanceOf[BSONWriter[T, BSONValue]],
    readDerivation.dispatch(sealedTrait.asInstanceOf[SealedTrait[readDerivation.Typeclass,T]]).asInstanceOf[BSONReader[BSONValue, T]]
  )

  def deriveMagnoliaHandler[T]: BSONDocumentHandler[T] = macro Magnolia.gen[T]
}

//object semiauto extends semiauto
