package it.unibo.pcd.assignment3.actors.controller.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.actors.controller.actors.Command._
import it.unibo.pcd.assignment3.actors.model.entities.StopwordsSet
import it.unibo.pcd.assignment3.actors.AnyOps.AnyOps

import scala.reflect.ClassTag

/** An actor which coordinates the distribution of the workload between the workers that registered to it and manage their
  * lifecycle.
  */
object CoordinatorActor {

  /** Returns the behavior of a generic coordinator actor.
    * @param root
    *   the root actor of the system
    * @param nextCoordinator
    *   the next coordinator actor in the data transformation chain
    * @tparam A
    *   the subtype of [[Command]] containing the data to be supplied as input to the worker actors
    * @return
    *   the behavior of a generic coordinator actor
    */
  def apply[A <: Command: ClassTag](root: ActorRef[Command], nextCoordinator: ActorRef[Command]): Behavior[Command] =
    Behaviors.setup { _ =>
      root ! Ready
      main(nextCoordinator, Map.empty[ActorRef[Command], Int])
    }

  /* The main state of a generic coordinator actor behavior. */
  private def main[A <: Command: ClassTag](
    nextCoordinator: ActorRef[Command],
    workers: Map[ActorRef[Command], Int]
  ): Behavior[Command] =
    Behaviors.receiveMessage {
      case Available(a) =>
        workers
          .get(a)
          .map(n => main(nextCoordinator, workers + (a -> (n - 1))))
          .getOrElse {
            a ! Ready
            main(nextCoordinator, workers + (a -> 0))
          }
      case PoisonPill => closed(nextCoordinator, workers)
      case a: A =>
        workers
          .minByOption(_._2)
          .map(_._1)
          .map(w => {
            w ! a
            main(nextCoordinator, workers + (w -> (workers(w) + 1)))
          })
          .getOrElse(Behaviors.unhandled)
      case _ => Behaviors.unhandled
    }

  /** Returns the behavior of a PageCoordinator actor.
    * @param root
    *   the root actor of the system
    * @param updateCoordinator
    *   the UpdateCoordinator next in line in the data transformation chain
    * @return
    *   the behavior of a PageCoordinator actor
    */
  def pageCoordinator(root: ActorRef[Command], updateCoordinator: ActorRef[Command]): Behavior[Command] = Behaviors.setup { _ =>
    root ! Ready
    awaitStopwords(updateCoordinator, Map.empty[ActorRef[Command], Int])
  }

  /* PageCoordinator behavior for waiting the receipt of the stopwords set and then send it to the workers already registered
   * before the stopwords arrival.
   */
  private def awaitStopwords(updateCoordinator: ActorRef[Command], workers: Map[ActorRef[Command], Int]): Behavior[Command] =
    Behaviors.receive { (c, m) =>
      m match {
        case StopwordsSetCommand(s, a) =>
          workers.keys.foreach(_ ! StopwordsSetCommand(s, c.self))
          awaitWorkers(updateCoordinator, workers, s, a, workers.size)
        case Available(a) =>
          a ! Ready
          awaitStopwords(updateCoordinator, workers + (a -> 0))
        case _ => Behaviors.unhandled
      }
    }

  /* PageCoordinator behavior for waiting the StopwordsAck command from all worker actors to which the stopwords set was sent
   * in the previous actor state.
   */
  private def awaitWorkers(
    updateCoordinator: ActorRef[Command],
    workers: Map[ActorRef[Command], Int],
    stopwordsSet: StopwordsSet,
    pathGeneratorActor: ActorRef[Command],
    remainingWorkers: Int
  ): Behavior[Command] =
    Behaviors.receive { (c, m) =>
      m match {
        case StopwordsAck(_) if remainingWorkers > 1 =>
          awaitWorkers(updateCoordinator, workers, stopwordsSet, pathGeneratorActor, remainingWorkers - 1)
        case StopwordsAck(_) =>
          pathGeneratorActor ! StopwordsAck(c.self)
          pageCoordinatorMain(updateCoordinator, workers, stopwordsSet)
        case Available(a) =>
          a ! Ready
          a ! StopwordsSetCommand(stopwordsSet, c.self)
          awaitWorkers(updateCoordinator, workers + (a -> 0), stopwordsSet, pathGeneratorActor, remainingWorkers + 1)
        case _ => Behaviors.unhandled
      }
    }

  /* The main state of a PageCoordinator actor behavior. */
  private def pageCoordinatorMain(
    updateCoordinator: ActorRef[Command],
    workers: Map[ActorRef[Command], Int],
    stopwordsSet: StopwordsSet
  ): Behavior[Command] = Behaviors.receive { (c, m) =>
    m match {
      case Available(a) =>
        workers
          .get(a)
          .map(n => pageCoordinatorMain(updateCoordinator, workers + (a -> (n - 1)), stopwordsSet))
          .getOrElse {
            a ! Ready
            a ! StopwordsSetCommand(stopwordsSet, c.self)
            Behaviors.same
          }
      case StopwordsAck(a) => pageCoordinatorMain(updateCoordinator, workers + (a -> 0), stopwordsSet)
      case PoisonPill      => closed(updateCoordinator, workers)
      case p: PageCommand =>
        workers
          .minByOption(_._2)
          .map(_._1)
          .map { w =>
            w ! p
            pageCoordinatorMain(updateCoordinator, workers + (w -> (workers(w) + 1)), stopwordsSet)
          }
          .getOrElse(Behaviors.unhandled)
      case _ => Behaviors.unhandled
    }
  }

  /* The behavior state in which all coordinators transition to when they receive a PoisonPill message, dedicated to shut down
   * all worker actors and the pass the PoisonPill to the next coordinator actor in the line when all workers have been killed.
   */
  private def closed(nextCoordinator: ActorRef[Command], workers: Map[ActorRef[Command], Int]): Behavior[Command] = {
    val actorsToBePoisoned: Iterable[ActorRef[Command]] = workers.filter(e => e._2 === 0).keys
    actorsToBePoisoned.foreach(_ ! PoisonPill)
    if (actorsToBePoisoned.size === workers.size) {
      nextCoordinator ! PoisonPill
    }
    onClosedAvailableReceived(nextCoordinator, workers -- actorsToBePoisoned)
  }

  /* The sub-state of the "closed" behavior state in which the coordinator actors awaits for new Available commands from its
   * worker actors, irregardless if they have already been registered before entering the "closed" state or are trying to
   * registered with this very command.
   */
  private def onClosedAvailableReceived(
    nextCoordinator: ActorRef[Command],
    workers: Map[ActorRef[Command], Int]
  ): Behavior[Command] =
    Behaviors.receiveMessage {
      case Available(a) =>
        workers
          .get(a)
          .map(n =>
            if (n > 1) {
              onClosedAvailableReceived(nextCoordinator, workers + (a -> (n - 1)))
            } else {
              a ! PoisonPill
              if (workers.size > 1) {
                onClosedAvailableReceived(nextCoordinator, workers - a)
              } else {
                nextCoordinator ! PoisonPill
                Behaviors.same[Command]
              }
            }
          )
          .getOrElse {
            a ! PoisonPill
            Behaviors.same[Command]
          }
      case _ => Behaviors.unhandled
    }
}
