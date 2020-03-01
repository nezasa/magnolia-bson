package magnolia.bson.derivation.handler

import magnolia.bson.BSONReadDerivation.OptionReader
import magnolia.bson.{BSONReadDerivation, BSONWriteDerivation}
import magnolia.{CaseClass, Magnolia, SealedTrait}
import reactivemongo.bson.{BSONDocumentHandler, BSONDocumentReader, BSONDocumentWriter, BSONHandler, BSONValue, BSONWriter, VariantBSONWriter, VariantBSONWriterWrapper}

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
  
  //workaround while https://github.com/ReactiveMongo/ReactiveMongo/pull/945 is not merged
  implicit def findWriter2[T, B <: BSONValue](implicit writer: VariantBSONWriter[T, B]): BSONWriter[T, B] =
    new VariantBSONWriterWrapper(writer)

  //another workaround
  class OptionHandler[T, B <: BSONValue](private val handler: BSONHandler[B, T]) extends OptionReader[T, B](handler) with BSONHandler[B, Option[T]] {
    override def write(t: Option[T]): B = writeDerivation.optionWriter(handler).write(t)
    override def read(bson: B): Option[T] = readDerivation.optionReader(handler).read(bson)
  }
  implicit def optionHandler[T, B <: BSONValue](implicit handler: BSONHandler[B, T]): BSONHandler[B, Option[T]] = new OptionHandler(handler)
}

//object semiauto extends semiauto
