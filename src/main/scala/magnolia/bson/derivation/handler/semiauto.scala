package magnolia.bson.derivation.handler

import magnolia.bson.{BSONReadDerivation, BSONWriteDerivation}
import magnolia.{CaseClass, Magnolia, SealedTrait}
import reactivemongo.bson.{BSONDocumentHandler, BSONDocumentReader, BSONDocumentWriter, BSONHandler, BSONReader, BSONValue, BSONWriter}

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object semiauto {

  type Typeclass[T] = BSONHandler[_ <: BSONValue, T]

  val readDerivation = BSONReadDerivation
  val writeDerivation = BSONWriteDerivation

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = BSONDocumentHandler[T](
    readDerivation.combine(caseClass.asInstanceOf[CaseClass[readDerivation.Typeclass,T]]).asInstanceOf[BSONDocumentReader[T]].read,
    writeDerivation.combine(caseClass.asInstanceOf[CaseClass[writeDerivation.Typeclass,T]]).asInstanceOf[BSONDocumentWriter[T]].write
  )

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = BSONDocumentHandler[T](
    readDerivation.dispatch(sealedTrait.asInstanceOf[SealedTrait[readDerivation.Typeclass,T]]).asInstanceOf[BSONDocumentReader[T]].read,
    writeDerivation.dispatch(sealedTrait.asInstanceOf[SealedTrait[writeDerivation.Typeclass,T]]).asInstanceOf[BSONDocumentWriter[T]].write,
  )

  def deriveMagnoliaHandler[T]: BSONDocumentHandler[T] = macro ExportedMagnolia.materializeImpl[T]
  // Wrap the output of Magnolia in an Exported to force it to a lower priority.
  // This seems to work, despite magnolia hardcode checks for `macroApplication` symbol
  // and relying on getting a diverging implicit expansion error for auto-mode.
  // Thankfully at least it doesn't check the output type of its `macroApplication`
  object ExportedMagnolia {
    def materializeImpl[A](c: whitebox.Context)(implicit t: c.WeakTypeTag[A]): c.Expr[BSONDocumentHandler[A]] = {
      val magnoliaTree = c.Expr[Typeclass[A]](Magnolia.gen[A](c))
      c.universe.reify(magnoliaTree.splice.asInstanceOf[BSONDocumentHandler[A]])
    }
  }
}

//object semiauto extends semiauto
