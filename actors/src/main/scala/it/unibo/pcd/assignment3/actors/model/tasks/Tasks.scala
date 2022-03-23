package it.unibo.pcd.assignment3.actors.model.tasks

import it.unibo.pcd.assignment3.actors.model.entities._

import java.nio.file.{Files, Path}

/** A task that can be executed by an actor as part of its behavior. It is a function than transforms an input resource of type A
  * into an output resource of type B.
  * @tparam A
  *   the type of the resources this task takes as input
  * @tparam B
  *   the type of the resources this task produces as output
  */
sealed trait Task[A, B] extends (A => B)

/** An extension of the [[Task]] trait which produce only one output resource after the transformation of an input resource.
  *
  * @tparam A
  *   the type of the resources this task takes as input
  * @tparam B
  *   the type of the resources this task produces as output
  */
sealed trait SingletonTask[A, B] extends Task[A, B]

/** An extension of the [[Task]] trait which produce an [[Iterable]] of output resources after the transformation of an input
  * resource.
  *
  * @tparam A
  *   the type of the resources this task takes as input
  * @tparam B
  *   the type of the resources this task produces [[Iterable]]s as output
  */
sealed trait IterableTask[A, B] extends Task[A, Iterable[B]]

/** A [[SingletonTask]] for generating the [[StopwordsSet]] given the [[FilePath]] of the file containing it. */
case object StopwordsGeneratorTask extends SingletonTask[FilePath, StopwordsSet] {

  import scala.jdk.CollectionConverters._

  /** The transformation function which converts a [[FilePath]] into a [[StopwordsSet]].
    * @param filePath
    *   the input [[FilePath]]
    * @return
    *   the output [[StopwordsSet]]
    */
  def apply(filePath: FilePath): StopwordsSet = StopwordsSet(Files.readAllLines(filePath.path).asScala.toSet)
}

/** An [[IterableTask]] for generating the [[FilePath]]s of the PDF documents given the [[FilePath]] of the folder containing
  * them.
  */
case object DocumentPathsGeneratorTask extends IterableTask[FilePath, FilePath] {

  import scala.jdk.StreamConverters._

  /** The transformation function which converts a [[FilePath]] into an [[Iterable]] of [[FilePath]]s.
    * @param filePath
    *   the input [[FilePath]]
    * @return
    *   the output [[Iterable]] of [[FilePath]]s
    */
  def apply(filePath: FilePath): Iterable[FilePath] =
    Files
      .list(filePath.path)
      .toScala(Seq.iterableFactory[Path])
      .filter(p => ".*pdf$".r.matches(p.toString))
      .map(FilePath(_))
}

/** A [[SingletonTask]] for transforming a [[FilePath]] of a PDF document into the [[Document]] itself. */
case object PathFilterTask extends SingletonTask[FilePath, Document] {

  import org.apache.pdfbox.pdmodel.PDDocument

  /** The transformation function which converts a [[FilePath]] into a [[Document]].
    * @param filePath
    *   the input [[FilePath]]
    * @return
    *   the output [[Document]]
    */
  override def apply(filePath: FilePath): Document = Document(PDDocument.load(filePath.path.toFile))
}

/** An [[IterableTask]] for transforming a [[Document]] into an [[Iterable]] of the [[Page]]s that constitute it. */
case object DocumentFilterTask extends IterableTask[Document, Page] {

  import org.apache.pdfbox.text.PDFTextStripper

  /** The transformation function which converts a [[Document]] into an [[Iterable]] of [[Page]]s.
    * @param document
    *   the input [[Document]]
    * @return
    *   the output [[Iterable]] of [[Page]]s
    */
  override def apply(document: Document): Iterable[Page] = {
    val stripper = new PDFTextStripper()
    val pages = (1 to document.document.getNumberOfPages)
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

/** A [[SingletonTask]] for transforming a [[Resource]] into a partial [[Update]] containing the frequency for each word and the
  * number of words contained into the [[Page]] of the [[Resource]].
  */
case object PageFilterTask extends SingletonTask[Resource, Update] {

  /** The transformation function which converts a [[Resource]] into an [[Update]].
    * @param resource
    *   the input [[Resource]]
    * @return
    *   the output [[Update]]
    */
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
