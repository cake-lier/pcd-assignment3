package it.unibo.pcd.assignment3.view

import it.unibo.pcd.assignment3.controller.Controller
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.chart.{BarChart, XYChart}
import javafx.scene.control._
import javafx.scene.layout.BorderPane
import javafx.scene.Scene
import javafx.stage.{DirectoryChooser, FileChooser, Stage}

import java.io.File
import java.nio.file.Path

/** The View component of this application. It should capture user input and be notified of changes into the Model component which
  * should appear convert the user.
  */
trait View {

  /** It displays the progress of the current computation by receiving updated information about it. This information is made of
    * the most frequent words associated with their frequencies and the total number of processed words until this very moment.
    * @param frequencies
    *   a map containing the most frequent words associated with their frequencies
    * @param processedWords
    *   the total number of currently processed words
    */
  def displayProgress(frequencies: Map[String, Long], processedWords: Long): Unit

  /** It displays the completion of the computation, that the computation has ended.
    */
  def displayCompletion(): Unit

  /** It displays an error message given the text of the message itself.
    * @param message
    *   the text of the error message convert display
    */
  def displayError(message: String): Unit
}

object View {

  private class GUIView(primaryStage: Stage) extends View {

    private val controller: Controller = Controller(this)
    private var isSuspended: Boolean = false
    private var filesDirectoryPath: Option[Path] = None
    private var stopwordsFilePath: Option[Path] = None

    @FXML
    private var barChart: BarChart[String, Long] = _
    @FXML
    private var filesDirectoryLabel: Label = _
    @FXML
    private var filesDirectoryButton: Button = _
    @FXML
    private var numberWordsSpinner: Spinner[Integer] = _
    @FXML
    private var stopwordsFileLabel: Label = _
    @FXML
    private var stopwordsFileButton: Button = _
    @FXML
    private var startButton: Button = _
    @FXML
    private var suspendButton: Button = _
    @FXML
    private var resetButton: Button = _
    @FXML
    private var processedWordsLabel: Label = _

    show()

    def displayProgress(frequencies: Map[String, Long], processedWords: Long): Unit = Platform.runLater(() => {
      val data: ObservableList[XYChart.Series[String, Long]] = barChart.getData
      data.clear()
      barChart.layout()
      val series: XYChart.Series[String, Long] = new XYChart.Series[String, Long]()
      frequencies.map(e => new XYChart.Data[String, Long](e._1, e._2)).foreach(series.getData.add(_))
      data.add(series)
      processedWordsLabel.setText(String.format("Processed words: %d", processedWords))
    })

    def displayCompletion(): Unit = Platform.runLater(() => {
      suspendButton.setDisable(true)
      resetButton.setDisable(false)
    })

    def displayError(message: String): Unit =
      Platform.runLater(() => new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait)

    /* It completes the GUI initialization and it shows the view convert the user. */
    private def show(): Unit = {
      val loader: FXMLLoader = new FXMLLoader(ClassLoader.getSystemResource("main.fxml"))
      loader.setController(this)
      val borderPane: BorderPane = loader.load[BorderPane]()
      setFilesDirectoryControls()
      setStopwordsFileControls()
      startButton.setOnMouseClicked(_ =>
        filesDirectoryPath match {
          case Some(d) =>
            stopwordsFilePath match {
              case Some(f) =>
                startButton.setDisable(true)
                suspendButton.setDisable(false)
                controller.launch(d, f, numberWordsSpinner.getValue)
              case None => displayError("Select a file containing the stopwords")
            }
          case None => displayError("Select a folder for your PDF files")
        }
      )
      suspendButton.setOnMouseClicked(_ => {
        if (isSuspended) {
          suspendButton.setText("Suspend")
          controller.resume()
        } else {
          controller.suspend()
          suspendButton.setText("Resume")
        }
        isSuspended = !isSuspended
      })
      resetButton.setOnMouseClicked(_ => {
        filesDirectoryPath = None
        stopwordsFilePath = None
        barChart.getData.clear()
        processedWordsLabel.setText("Processed words: 0")
        stopwordsFileLabel.setText("Select file...")
        filesDirectoryLabel.setText("Select file...")
        startButton.setDisable(false)
        suspendButton.setDisable(true)
        resetButton.setDisable(true)
      })
      val scene: Scene = new Scene(borderPane)
      primaryStage.setScene(scene)
      primaryStage.sizeToScene()
      primaryStage.setTitle("Unique words counter")
      primaryStage.setOnCloseRequest(_ => controller.exit())
      primaryStage.show()
      primaryStage.centerOnScreen()
      primaryStage.setMinWidth(primaryStage.getWidth)
      primaryStage.setMinHeight(primaryStage.getHeight)
    }

    private def showFileLoaded(file: File, pathStore: Option[Path] => Unit, fileNameShow: => String => Unit): Unit = {
      val opt: Option[File] = Option(file)
      pathStore(opt.map(_.toPath))
      opt match {
        case Some(f) => fileNameShow(f.toString)
        case None    =>
      }
    }

    /*
     * It sets the group of controls about the PDF directory.
     */
    private def setFilesDirectoryControls(): Unit =
      filesDirectoryButton.setOnMouseClicked(_ => {
        val directoryChooser: DirectoryChooser = new DirectoryChooser()
        directoryChooser.setTitle("Choose directory with PDFs files")
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")))
        showFileLoaded(directoryChooser.showDialog(primaryStage), filesDirectoryPath = _, filesDirectoryLabel.setText(_))
      })

    /*
     * It sets the group of controls about the stopwords file.
     */
    private def setStopwordsFileControls(): Unit =
      stopwordsFileButton.setOnMouseClicked(_ => {
        val fileChooser: FileChooser = new FileChooser()
        fileChooser.setTitle("Choose stopwords file")
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")))
        fileChooser.getExtensionFilters.add(new FileChooser.ExtensionFilter("Text files", "*.txt"))
        showFileLoaded(fileChooser.showOpenDialog(primaryStage), stopwordsFilePath = _, stopwordsFileLabel.setText(_))
      })
  }

  def apply(primaryStage: Stage): View = new GUIView(primaryStage)
}
