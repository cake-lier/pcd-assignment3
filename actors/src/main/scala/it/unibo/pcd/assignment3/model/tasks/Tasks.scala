package it.unibo.pcd.assignment3.model.tasks

import it.unibo.pcd.assignment3.model.entities._

import java.nio.file.{Files, Path}

sealed trait Task[A, B] extends (A => B)

sealed trait SingletonTask[A, B] extends Task[A, B]

sealed trait IterableTask[A, B] extends Task[A, Iterable[B]]

case object StopwordsGeneratorTask extends SingletonTask[FilePath, StopwordsSet] {

  import scala.jdk.CollectionConverters._

  def apply(filePath: FilePath): StopwordsSet = StopwordsSet(Files.readAllLines(filePath.path).asScala.toSet)
}

case object DocumentPathsGeneratorTask extends IterableTask[FilePath, FilePath] {

  import scala.jdk.StreamConverters._

  def apply(filePath: FilePath): Iterable[FilePath] =
    Files
      .list(filePath.path)
      .toScala(Seq.iterableFactory[Path])
      .filter(p => ".*pdf$".r.matches(p.toString))
      .map(FilePath(_))
}

case object PathFilterTask extends SingletonTask[FilePath, Document] {

  import org.apache.pdfbox.pdmodel.PDDocument

  override def apply(filePath: FilePath): Document = Document(PDDocument.load(filePath.path.toFile))
}

case object DocumentFilterTask extends IterableTask[Document, Page] {

  import org.apache.pdfbox.text.PDFTextStripper

  override def apply(document: Document): Iterable[Page] = {
    val stripper: PDFTextStripper = new PDFTextStripper()
    val pages: Iterable[Page] = (1 to document.document.getNumberOfPages)
      .map(i => {
        stripper.setStartPage(i)
        stripper.setEndPage(i)
        stripper.getText(document.document)
      })
      .map(Page(_))
    document.document.close()
    pages
  }
}

case object PageFilterTask extends SingletonTask[Resource, Update] {

  def apply(resource: Resource): Update = {
    val words = "\\W+".r.split(resource.page.text).toSeq
    Update(
      words
        .map(_.toLowerCase)
        .filter(!resource.stopwordsSet.stopwords.contains(_))
        .groupBy(w => w)
        .map(e => (e._1, e._2.length)),
      words.length
    )
  }
}
