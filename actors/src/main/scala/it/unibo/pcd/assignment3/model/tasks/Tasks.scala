package it.unibo.pcd.assignment3.model.tasks

import it.unibo.pcd.assignment3.model.entities.{Command, FilePath, Page, Resource, StopwordsSet, Update}

import java.nio.file.{Files, Path}
import scala.util.Using

sealed trait Task[A <: Command, B] extends (A => B)

sealed trait SingletonTask[A <: Command, B <: Command] extends Task[A, B]

sealed trait IterableTask[A <: Command, B <: Command] extends Task[A, Iterable[B]]

sealed trait StopwordsGeneratorTask extends SingletonTask[FilePath, StopwordsSet]

object StopwordsGeneratorTask {

  private final class StopwordsGeneratorTaskImpl extends StopwordsGeneratorTask {

    import scala.jdk.CollectionConverters._

    def apply(filePath: FilePath): StopwordsSet = StopwordsSet(Files.readAllLines(filePath.path).asScala.toSet)
  }

  def apply(): StopwordsGeneratorTask = new StopwordsGeneratorTaskImpl()
}

sealed trait DocumentPathsGeneratorTask extends IterableTask[FilePath, FilePath]

object DocumentPathsGeneratorTask {

  private final class DocumentPathsGeneratorTaskImpl extends DocumentPathsGeneratorTask {

    import scala.jdk.StreamConverters._

    def apply(filePath: FilePath): Iterable[FilePath] =
      Files
        .list(filePath.path)
        .toScala(Seq.iterableFactory[Path])
        .filter(p => ".*pdf$".r.matches(p.toString))
        .map(FilePath(_))
  }

  def apply(): DocumentPathsGeneratorTask = new DocumentPathsGeneratorTaskImpl()
}

sealed trait PathFilterTask extends IterableTask[FilePath, Page]

object PathFilterTask {

  private final class PathFilterTaskImpl extends PathFilterTask {

    import org.apache.pdfbox.pdmodel.PDDocument
    import org.apache.pdfbox.text.PDFTextStripper

    override def apply(filePath: FilePath): Iterable[Page] = {
      val stripper: PDFTextStripper = new PDFTextStripper()
      Using(
        PDDocument.load(filePath.path.toFile)
      )(d =>
        (1 to d.getNumberOfPages)
          .map(i => {
            stripper.setStartPage(i)
            stripper.setEndPage(i)
            stripper.getText(d)
          })
          .map(Page(_))
      ).getOrElse(Seq.empty[Page])
    }
  }

  def apply(): PathFilterTask = new PathFilterTaskImpl()
}

sealed trait PageFilterTask extends SingletonTask[Resource, Update]

object PageFilterTask {

  private final class PageFilterTaskImpl extends PageFilterTask {

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

  def apply(): PageFilterTask = new PageFilterTaskImpl()
}
