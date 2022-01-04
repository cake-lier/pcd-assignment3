package it.unibo.pcd.assignment3.puzzleactors.controller

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.Address
import akka.cluster.typed.{Cluster, Join}
import com.typesafe.config.{Config, ConfigFactory}
import it.unibo.pcd.assignment3.puzzleactors.controller.Command.{NewBoardReceived, SwapRequest}
import it.unibo.pcd.assignment3.puzzleactors.model.{Position, PuzzleBoard, Swap}
import it.unibo.pcd.assignment3.puzzleactors.view.View

trait Controller {

  def exit(): Unit

  def swap(firstPosition: Position, secondPosition: Position): Unit
}

object Controller {

  private class ControllerImpl(actorSystem: ActorSystem[Command]) extends Controller {

    override def exit(): Unit = {
      actorSystem.terminate()
      sys.exit()
    }

    override def swap(firstPosition: Position, secondPosition: Position): Unit =
      actorSystem ! SwapRequest(Swap(firstPosition, secondPosition))
  }

  private val clusterSystemName = "puzzle"

  def apply(rows: Int, columns: Int, view: View)(host: String, port: Int): Controller = new ControllerImpl(
    ActorSystem[Command](
      Behaviors.setup(c => {
        val puzzleBoard: PuzzleBoard = PuzzleBoard(rows, columns)
        val cluster = Cluster(c.system)
        cluster.manager ! Join(cluster.selfMember.address)
        val peer = c.spawnAnonymous[Command](Peer(c.self, puzzleBoard))
        displayBoard(view, puzzleBoard)
        afterInitializationState(peer, view)
      }),
      clusterSystemName,
      createConfiguration(host, port)
    )
  )

  def apply(view: View)(host: String, port: Int, remoteHost: String, remotePort: Int): Controller = new ControllerImpl(
    ActorSystem[Command](
      Behaviors.setup(c => {
        val cluster = Cluster(c.system)
        val buddyAddress = Address("akka", clusterSystemName, remoteHost, remotePort)
        cluster.manager ! Join(buddyAddress)
        val peer = c.spawnAnonymous[Command](Peer(c.self))
        Behaviors.receiveMessage {
          case NewBoardReceived(b) =>
            displayBoard(view, b)
            afterInitializationState(peer, view)
          case _ => Behaviors.unhandled
        }
      }),
      clusterSystemName,
      createConfiguration(host, port)
    )
  )

  private def createConfiguration(host: String, port: Int): Config =
    ConfigFactory
      .parseString(
        s"""
           |akka.remote.artery.canonical {
           |  hostname = "$host"
           |  port = ${port.toString}
           |}
           |""".stripMargin
      )
      .withFallback(ConfigFactory.load("application_cluster"))

  private def afterInitializationState(peer: ActorRef[Command], view: View): Behavior[Command] =
    Behaviors.receiveMessage {
      case NewBoardReceived(b) =>
        displayBoard(view, b)
        Behaviors.same
      case s: SwapRequest =>
        peer ! s
        Behaviors.same
      case _ => Behaviors.unhandled
    }

  private def displayBoard(view: View, puzzleBoard: PuzzleBoard): Unit = {
    view.displayTiles(puzzleBoard.tiles)
    if (puzzleBoard.isSolution) {
      view.displaySolution()
    }
  }
}
