package it.unibo.pcd.assignment3.actors.model.entities

import org.apache.pdfbox.pdmodel.PDDocument

/** A document entity as conceived into the problem space.
  *
  * It must be constructed through its companion object.
  */
trait Document {

  /** Returns the wrapped document object as defined by the Apache PDFBox library. */
  val document: PDDocument
}

/** The companion object of the [[Document]] trait, containing its factory method. */
object Document {

  /* An implementation of the Document trait. */
  private final case class DocumentImpl(document: PDDocument) extends Document

  /** The factory method for creating new instances of the [[Document]] trait wrapping an instance of a document as defined by the
    * Apache PDFBox library.
    * @param document
    *   the wrapped document object as defined by the Apache PDFBox library
    * @return
    *   a new [[Document]] instance
    */
  def apply(document: PDDocument): Document = DocumentImpl(document)
}
