package it.unibo.pcd.assignment3.model.entities

import org.apache.pdfbox.pdmodel.PDDocument

trait Document {

  val document: PDDocument
}

object Document {

  private final case class DocumentImpl(document: PDDocument) extends Document

  def apply(document: PDDocument): Document = DocumentImpl(document)
}
