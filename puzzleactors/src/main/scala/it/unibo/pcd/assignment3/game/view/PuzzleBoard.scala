package it.unibo.pcd.assignment3.game.view

import it.unibo.pcd.assignment3.Operations.discard
import it.unibo.pcd.assignment3.game.TileButton
import it.unibo.pcd.assignment3.game.controller.SelectCard
import it.unibo.pcd.assignment3.game.model.{Actor, Board}

import java.awt.{BorderLayout, Color, GridLayout, Image}
import java.awt.image.{CropImageFilter, FilteredImageSource}
import javax.swing.{BorderFactory, JFrame, JOptionPane, JPanel, WindowConstants}

trait PuzzleBoard {
  def printBoard(board: Board): Unit
  def showGameCompletion(): Unit
}

object PuzzleBoard {
  def apply(rows: Int, columns: Int, image: Image, controller: Actor): PuzzleBoard =
    PuzzleBoardImpl(rows, columns, image, controller)

  private final case class PuzzleBoardImpl(rows: Int, columns: Int, image: Image, controller: Actor)
    extends JFrame
    with PuzzleBoard {

    private val panelBoard = new JPanel()
    private val buttons: Map[Int, TileButton] = createButtons()
    panelBoard.setBorder(BorderFactory.createLineBorder(Color.gray))
    panelBoard.setLayout(new GridLayout(rows, columns, 0, 0))
    getContentPane.add(panelBoard, BorderLayout.CENTER)

    setVisible(true)
    setTitle("Puzzle Game")
    setResizable(false)
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

    @SuppressWarnings(Array("org.wartremover.warts.Null"))
    private def createButtons(): Map[Int, TileButton] =
      (for (row <- 0 until rows; col <- 0 until columns) yield (row, col))
        .zip(0 until rows * columns)
        .map { case ((r, c), i) =>
          val imageWidth = image.getWidth(null)
          val imageHeight = image.getHeight(null)
          val imagePortion: Image = createImage(
            new FilteredImageSource(
              image.getSource,
              new CropImageFilter(c * imageWidth / columns, r * imageHeight / rows, imageWidth / columns, imageHeight / rows)
            )
          )
          i -> imagePortion
        }
        .map { case (index, image) =>
          val btn: TileButton = new TileButton(image)
          btn.addActionListener { _ => controller ! SelectCard(panelBoard.getComponents.indexOf(btn)) }
          index -> btn
        }
        .toMap

    override def printBoard(board: Board): Unit = {
      panelBoard.removeAll()
      board.foreach { card =>
        val button = buttons(card)
        discard { panelBoard.add(button) }
        button.setBorder(BorderFactory.createLineBorder(Color.gray))
      }
      pack()
    }

    override def showGameCompletion(): Unit = {
      JOptionPane.showMessageDialog(this, "Puzzle Completed!", "", JOptionPane.INFORMATION_MESSAGE)
    }
  }
}
