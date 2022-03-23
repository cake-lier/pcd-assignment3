package it.unibo.pcd.assignment3.puzzleactors.controller

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.Address
import akka.cluster.typed.{Cluster, Join}
import com.typesafe.config.{Config, ConfigFactory}
import it.unibo.pcd.assignment3.puzzleactors.controller.Command.{NewBoardReceived, SetupError, SwapRequest}
import it.unibo.pcd.assignment3.puzzleactors.model.{Position, PuzzleBoard, Swap}
import it.unibo.pcd.assignment3.puzzleactors.view.View

/** The Controller component of this application, it should represent the application itself. That being so, it receives user
  * input from the View component and notifies it of changes in the Model component state. It should also be capable of notifying
  * the Model of requests made by the user and receive the adequate response. At last, it should manage the application state.
  *
  * It must be constructed through its companion object.
  */
trait Controller {

  /** It exits the application. */
  def exit(): Unit

  /** Swaps the two [[it.unibo.pcd.assignment3.puzzleactors.model.Tile]] associated to the two given [[Position]]s in the player
    * [[PuzzleBoard]].
    * @param firstPosition
    *   the [[Position]] of the first [[it.unibo.pcd.assignment3.puzzleactors.model.Tile]] to be swapped
    * @param secondPosition
    *   the [[Position]] of the second [[it.unibo.pcd.assignment3.puzzleactors.model.Tile]] to be swapped
    */
  def swap(firstPosition: Position, secondPosition: Position): Unit
}

/** Companion object to the [[Controller]] trait, containing its factory methods. */
object Controller {

  /* An implementation of the Controller trait. */
  private class ControllerImpl(actorSystem: ActorSystem[Command]) extends Controller {

    override def exit(): Unit = {
      actorSystem.terminate()
      sys.exit()
    }

    override def swap(firstPosition: Position, secondPosition: Position): Unit =
      actorSystem ! SwapRequest(Swap(firstPosition, secondPosition))
  }

  private val clusterSystemName = "puzzle"

  /** The factory method for creating a new instance of the [[Controller]] trait when associated with the first peer joining a
    * cluster.
    * @param rows
    *   the number of rows in the [[PuzzleBoard]] used in the game
    * @param columns
    *   the number of columns in the [[PuzzleBoard]] used in the game
    * @param view
    *   the [[View]] component to be used by the constructed [[Controller]]
    * @param host
    *   the hostname of the peer associated with the constructed [[Controller]]
    * @param port
    *   the port of the peer associated with the constructed [[Controller]]
    * @return
    *   a new instance of the [[Controller]] trait
    */
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

  /** The factory method for creating a new instance of the [[Controller]] trait when associated with an "extra peer", a peer
    * which is not the first in joining a game session.
    * @param view
    *   the [[View]] component to be used by the constructed [[Controller]]
    * @param host
    *   the hostname of the peer associated with the constructed [[Controller]]
    * @param port
    *   the port of the peer associated with the constructed [[Controller]]
    * @param remoteHost
    *   the hostname of the "buddy peer", the peer to be contacted by the local peer for joining the desired session
    * @param remotePort
    *   the port of the "buddy peer", the peer to be contacted by the local peer for joining the desired session
    * @return
    *   a new instance of the [[Controller]] trait
    */
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

  /* Creates a new Akka Cluster configuration for the local peer given the default configuration stored in the settings file. */
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

  /* Returns the state of the root actor in the local actor system after its initialization state. */
  private def afterInitializationState(peer: ActorRef[Command], view: View): Behavior[Command] =
    Behaviors.receiveMessage {
      case NewBoardReceived(b) =>
        displayBoard(view, b)
        Behaviors.same
      case s: SwapRequest =>
        peer ! s
        Behaviors.same
      case SetupError =>
        view.displayJoinError()
        Behaviors.ignore
      case _ => Behaviors.unhandled
    }

  /* Displays a new PuzzleBoard Tiles arrangement on the View component, showing also a message if the arrangement is a
   * solution for the puzzle.
   */
  private def displayBoard(view: View, puzzleBoard: PuzzleBoard): Unit = {
    view.displayTiles(puzzleBoard.tiles)
    if (puzzleBoard.isSolution) {
      view.displaySolution()
    }
  }
}
