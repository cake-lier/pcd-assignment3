package it.unibo.pcd.assignment3.actors.controller.actors

import it.unibo.pcd.assignment3.actors.controller.actors.Command._
import it.unibo.pcd.assignment3.actors.model.entities._

/** Type-class for extending types and adding them the capability for being converted to and from a related [[Command]].
  * @tparam A
  *   the type to extend
  * @tparam B
  *   the corresponding [[Command]] subtype the type to extend is realted to
  */
trait ConvertibleToCommand[A, B <: Command] {

  /** Converts an instance of type A to an instance of subtype of [[Command]] B.
    * @param a
    *   the instance to convert
    * @return
    *   an instance of the corresponding subtype of [[Command]]
    */
  def to(a: A): B

  /** Converts an instance of subtype of [[Command]] B back into an instance of type A.
    * @param b
    *   the instance of subtype of [[Command]] to convert
    * @return
    *   the corresponding instance
    */
  def from(b: B): A
}

/** Companion object to the [[ConvertibleToCommand]] type-class, containing its instances and interfaces. */
object ConvertibleToCommand {

  /** An interface for the [[ConvertibleToCommand]] type-class allowing to extend the A type with an operation capable of
    * converting it to the corresponding subtype of [[Command]] B given the correct [[ConvertibleToCommand]] instance.
    * @param a
    *   the instance which type is to be extended with an operation capable of converting it to the corresponding subtype of
    *   [[Command]] B
    * @param converter
    *   the instance of the type-class [[ConvertibleToCommand]] to be used for the conversion
    * @tparam A
    *   the type of the instance to be extended
    * @tparam B
    *   the subtype of [[Command]] the instance has to be converted to
    */
  implicit class RichConvertibleToCommand[A, B <: Command](a: A)(implicit converter: ConvertibleToCommand[A, B]) {

    /** Converts this instance into an instance of the subtype of [[Command]] B.
      * @return
      *   an instance of the related subtype of [[Command]] B to this instance
      */
    def toCommand: B = converter.to(a)
  }

  /** An interface for the [[ConvertibleToCommand]] type-class allowing to extend the subtype of [[Command]] B with an operation
    * capable of converting it to the corresponding type A given the correct [[ConvertibleToCommand]] instance.
    * @param b
    *   the instance which type is to be extended with an operation capable of converting it to the corresponding type A
    * @param converter
    *   the instance of the type-class [[ConvertibleToCommand]] to be used for the conversion
    * @tparam A
    *   the type to which the subtype of [[Command]] has to be converted to
    * @tparam B
    *   the subtype [[Command]] to be extended
    */
  implicit class RichCommandConverted[A, B <: Command](b: B)(implicit converter: ConvertibleToCommand[A, B]) {

    /** Converts this instance of subtype of [[Command]] into an instance of type A.
      *
      * @return
      *   an instance of the related type A to this instance
      */
    def fromCommand: A = converter.from(b)
  }

  /** Instance of the [[ConvertibleToCommand]] type-class for converting a [[Document]] into a [[DocumentCommand]] and vice versa.
    */
  implicit object ConvertibleToCommandDocument extends ConvertibleToCommand[Document, DocumentCommand] {

    override def to(a: Document): DocumentCommand = DocumentCommand(a.document)

    override def from(b: DocumentCommand): Document = Document(b.document)
  }

  /** Instance of the [[ConvertibleToCommand]] type-class for converting a [[FilePath]] into a [[FilePathCommand]] and vice versa.
    */
  implicit object ConvertibleToCommandFilePath extends ConvertibleToCommand[FilePath, FilePathCommand] {

    override def to(a: FilePath): FilePathCommand = FilePathCommand(a.path)

    override def from(b: FilePathCommand): FilePath = FilePath(b.path)
  }

  /** Instance of the [[ConvertibleToCommand]] type-class for converting a [[Page]] into a [[PageCommand]] and vice versa. */
  implicit object ConvertibleToCommandPage extends ConvertibleToCommand[Page, PageCommand] {

    override def to(a: Page): PageCommand = PageCommand(a.text)

    override def from(b: PageCommand): Page = Page(b.text)
  }

  /** Instance of the [[ConvertibleToCommand]] type-class for converting an [[Update]] into an [[UpdateCommand]] and vice versa.
    */
  implicit object ConvertibleToCommandUpdate extends ConvertibleToCommand[Update, UpdateCommand] {

    override def to(a: Update): UpdateCommand = UpdateCommand(a.frequencies, a.processedWords)

    override def from(b: UpdateCommand): Update = Update(b.frequencies, b.processedWords)
  }
}
