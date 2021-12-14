package it.unibo.pcd.assignment3.game
import java.awt.{Color, Image}
import java.awt.event.{ActionListener, MouseAdapter, MouseEvent, MouseListener}
import javax.swing.{BorderFactory, ImageIcon, JButton}

class TileButton(tile: Image) extends JButton(new ImageIcon(tile)) {
  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      setBorder(BorderFactory.createLineBorder(Color.red))
    }
  })
}
