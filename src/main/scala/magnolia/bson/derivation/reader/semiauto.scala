package magnolia.bson.derivation.reader

import magnolia.bson.BSONReadDerivation
import magnolia.{CaseClass, Magnolia, SealedTrait, debug}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONReader, BSONValue, VariantBSONReader}

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object semiauto {

//  type B <: BSONValue
  private type Typeclass[T] = BSONReadDerivation.Typeclass[T]

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T]  =
    BSONReadDerivation.combine(caseClass)

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] =
    BSONReadDerivation.dispatch(sealedTrait)

  def deriveMagnoliaReader[T]: BSONDocumentReader[T] = macro ExportedMagnolia.materializeImpl[T]
  // Wrap the output of Magnolia in an Exported to force it to a lower priority.
  // This seems to work, despite magnolia hardcode checks for `macroApplication` symbol
  // and relying on getting a diverging implicit expansion error for auto-mode.
  // Thankfully at least it doesn't check the output type of its `macroApplication`
  object ExportedMagnolia {
    def materializeImpl[A](c: whitebox.Context)(implicit t: c.WeakTypeTag[A]): c.Expr[BSONDocumentReader[A]] = {
      val magnoliaTree = c.Expr[Typeclass[A]](Magnolia.gen[A](c))
      c.universe.reify(magnoliaTree.splice.asInstanceOf[BSONDocumentReader[A]])
    }
  }
}

//object semiauto extends semiauto
