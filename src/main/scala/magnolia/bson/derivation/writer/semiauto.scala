package magnolia.bson.derivation.writer

import magnolia.bson.BSONWriteDerivation
import magnolia.{CaseClass, Magnolia, SealedTrait}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONValue, BSONWriter}

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object semiauto {

//  type B <: BSONValue
  private type Typeclass[T] = BSONWriteDerivation.Typeclass[T]
//
//  val d = new BSONWriteDerivation[B] {}

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] =
    BSONWriteDerivation.combine(caseClass)

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] =
    BSONWriteDerivation.dispatch(sealedTrait)

  def deriveMagnoliaWriter[T]: BSONDocumentWriter[T] = macro ExportedMagnolia.materializeImpl[T]
  // Wrap the output of Magnolia in an Exported to force it to a lower priority.
  // This seems to work, despite magnolia hardcode checks for `macroApplication` symbol
  // and relying on getting a diverging implicit expansion error for auto-mode.
  // Thankfully at least it doesn't check the output type of its `macroApplication`
  object ExportedMagnolia {
    def materializeImpl[A](c: whitebox.Context)(implicit t: c.WeakTypeTag[A]): c.Expr[BSONDocumentWriter[A]] = {
      val magnoliaTree = c.Expr[Typeclass[A]](Magnolia.gen[A](c))
      c.universe.reify(magnoliaTree.splice.asInstanceOf[BSONDocumentWriter[A]])
    }
  }
}
