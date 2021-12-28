package it.unibo.pcd.assignment3.actors.controller.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.actors.controller.actors.Command._
import it.unibo.pcd.assignment3.actors.controller.actors.ConvertibleToCommand.{RichCommandConverted, RichConvertibleToCommand}
import it.unibo.pcd.assignment3.actors.model.entities.{Page, Resource, StopwordsSet}
import it.unibo.pcd.assignment3.actors.AnyOps.AnyOps

import scala.reflect.ClassTag

object CoordinatorActor {

  def apply[A <: Command: ClassTag](root: ActorRef[Command], nextCoordinator: ActorRef[Command]): Behavior[Command] =
    Behaviors.setup { _ =>
      root ! Ready
      main(nextCoordinator, Map.empty[ActorRef[Command], Int])
    }

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

  def pageCoordinator(
    root: ActorRef[Command],
    updateCoordinator: ActorRef[Command]
  )(implicit
    pageConverter: ConvertibleToCommand[Page, PageCommand],
    resourceConverter: ConvertibleToCommand[Resource, ResourceCommand]
  ): Behavior[Command] =
    Behaviors.setup { _ =>
      root ! Ready
      pageCoordinatorMain(updateCoordinator, Map.empty[ActorRef[Command], Int], None)
    }

  private def pageCoordinatorMain(
    updateCoordinator: ActorRef[Command],
    workers: Map[ActorRef[Command], Int],
    stopwordsSet: Option[StopwordsSet]
  )(implicit
    pageConverter: ConvertibleToCommand[Page, PageCommand],
    resourceConverter: ConvertibleToCommand[Resource, ResourceCommand]
  ): Behavior[Command] =
    Behaviors.receiveMessage {
      case StopwordsSetCommand(s, r) =>
        r ! StopwordsAck
        pageCoordinatorMain(updateCoordinator, workers, Some(s))
      case Available(a) =>
        workers
          .get(a)
          .map(n => pageCoordinatorMain(updateCoordinator, workers + (a -> (n - 1)), stopwordsSet))
          .getOrElse {
            a ! Ready
            pageCoordinatorMain(updateCoordinator, workers + (a -> 0), stopwordsSet)
          }
      case PoisonPill => closed(updateCoordinator, workers)
      case p: PageCommand =>
        workers
          .minByOption(_._2)
          .map(_._1)
          .flatMap(w =>
            stopwordsSet.map(s => {
              w ! Resource(p.fromCommand, s).toCommand
              pageCoordinatorMain(updateCoordinator, workers + (w -> (workers(w) + 1)), stopwordsSet)
            })
          )
          .getOrElse(Behaviors.unhandled)
      case _ => Behaviors.unhandled
    }

  private def closed(nextCoordinator: ActorRef[Command], workers: Map[ActorRef[Command], Int]): Behavior[Command] = {
    val actorsToBePoisoned: Iterable[ActorRef[Command]] = workers.filter(e => e._2 === 0).keys
    actorsToBePoisoned.foreach(_ ! PoisonPill)
    if (actorsToBePoisoned.size === workers.size) {
      nextCoordinator ! PoisonPill
    }
    onClosedAvailableReceived(nextCoordinator, workers -- actorsToBePoisoned)
  }

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