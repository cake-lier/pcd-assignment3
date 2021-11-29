package it.unibo.pcd.assignment3.model.entities

import org.apache.pdfbox.pdmodel.PDDocument

/** A document entity as conceived into the problem space.
  */
trait Document {

  /** Returns the wrapped document object as defined by the Apache PDFBox library. */
  val document: PDDocument
}

object Document {

  private final case class DocumentImpl(document: PDDocument) extends Document

  def apply(document: PDDocument): Document = DocumentImpl(document)
}
