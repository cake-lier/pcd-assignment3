package it.unibo.pcd.assignment3.controller.actors

import it.unibo.pcd.assignment3.controller.actors.Command._
import it.unibo.pcd.assignment3.model.entities._

trait ConvertibleToCommand[A, B <: Command] {

  def to(a: A): B

  def from(b: B): A
}

object ConvertibleToCommand {

  implicit class RichConvertibleToCommand[A, B <: Command](a: A)(implicit converter: ConvertibleToCommand[A, B]) {

    def toCommand: B = converter.to(a)
  }

  implicit class RichCommandConverted[A, B <: Command](b: B)(implicit converter: ConvertibleToCommand[A, B]) {

    def fromCommand: A = converter.from(b)
  }

  implicit object ConvertibleToCommandDocument extends ConvertibleToCommand[Document, DocumentCommand] {

    def to(a: Document): DocumentCommand = DocumentCommand(a.document)

    def from(b: DocumentCommand): Document = Document(b.document)
  }

  implicit object ConvertibleToCommandFilePath extends ConvertibleToCommand[FilePath, FilePathCommand] {

    def to(a: FilePath): FilePathCommand = FilePathCommand(a.path)

    def from(b: FilePathCommand): FilePath = FilePath(b.path)
  }

  implicit object ConvertibleToCommandPage extends ConvertibleToCommand[Page, PageCommand] {

    def to(a: Page): PageCommand = PageCommand(a.text)

    def from(b: PageCommand): Page = Page(b.text)
  }

  implicit object ConvertibleToCommandResource extends ConvertibleToCommand[Resource, ResourceCommand] {

    def to(a: Resource): ResourceCommand = ResourceCommand(a.page, a.stopwordsSet)

    def from(b: ResourceCommand): Resource = Resource(b.page, b.stopwordsSet)
  }

  implicit object ConvertibleToCommandUpdate extends ConvertibleToCommand[Update, UpdateCommand] {

    def to(a: Update): UpdateCommand = UpdateCommand(a.frequencies, a.processedWords)

    def from(b: UpdateCommand): Update = Update(b.frequencies, b.processedWords)
  }
}
