package it.unibo.pcd.assignment3.controller.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.controller.actors.Command.{
  Available,
  PageCommand,
  PoisonPill,
  ResourceCommand,
  StopwordsAck,
  StopwordsSetCommand
}
import it.unibo.pcd.assignment3.controller.actors.ConvertibleToCommand.{RichCommandConverted, RichConvertibleToCommand}
import it.unibo.pcd.assignment3.model.entities.{Page, Resource, StopwordsSet}
import it.unibo.pcd.assignment3.AnyOps.AnyOps

import scala.reflect.ClassTag

object CoordinatorActor {

  def apply[A <: Command: ClassTag](): Behavior[Command] = apply(None, Map.empty[ActorRef[Command], Int])

  def apply[A <: Command: ClassTag](nextCoordinator: ActorRef[Command]): Behavior[Command] =
    apply(Some(nextCoordinator), Map.empty[ActorRef[Command], Int])

  private def apply[A <: Command: ClassTag](
    nextCoordinator: Option[ActorRef[Command]],
    workers: Map[ActorRef[Command], Int]
  ): Behavior[Command] =
    Behaviors.setup(_ =>
      Behaviors.receiveMessage {
        case Available(a) =>
          workers
            .get(a)
            .map(n => apply(nextCoordinator, workers + (a -> (n - 1))))
            .getOrElse(apply(nextCoordinator, workers + (a -> 0)))
        case PoisonPill =>
          val poisonedActors: Iterable[ActorRef[Command]] = workers.filter(e => e._2 === 0).keys
          poisonedActors.foreach(_ ! PoisonPill)
          closed(nextCoordinator, workers -- poisonedActors)
        case a: A =>
          workers
            .minByOption(_._2)
            .map(_._1)
            .map(w => {
              w ! a
              apply(nextCoordinator, workers + (w -> (workers(w) + 1)))
            })
            .getOrElse(Behaviors.unhandled)
        case _ => Behaviors.unhandled
      }
    )

  def pageCoordinator(
    updateCoordinator: ActorRef[Command]
  )(implicit
    pageConverter: ConvertibleToCommand[Page, PageCommand],
    resourceConverter: ConvertibleToCommand[Resource, ResourceCommand]
  ): Behavior[Command] =
    pageCoordinator(updateCoordinator, Map.empty[ActorRef[Command], Int], None)

  private def pageCoordinator(
    updateCoordinator: ActorRef[Command],
    workers: Map[ActorRef[Command], Int],
    stopwordsSet: Option[StopwordsSet]
  )(implicit
    pageConverter: ConvertibleToCommand[Page, PageCommand],
    resourceConverter: ConvertibleToCommand[Resource, ResourceCommand]
  ): Behavior[Command] =
    Behaviors.setup(_ =>
      Behaviors.receiveMessage {
        case StopwordsSetCommand(s, r) =>
          r ! StopwordsAck
          pageCoordinator(updateCoordinator, workers, Some(s))
        case Available(a) =>
          workers
            .get(a)
            .map(n => pageCoordinator(updateCoordinator, workers + (a -> (n - 1)), stopwordsSet))
            .getOrElse(pageCoordinator(updateCoordinator, workers + (a -> 0), stopwordsSet))
        case PoisonPill =>
          val poisonedActors: Iterable[ActorRef[Command]] = workers.filter(e => e._2 === 0).keys
          poisonedActors.foreach(_ ! PoisonPill)
          closed(Some(updateCoordinator), workers -- poisonedActors)
        case p: PageCommand =>
          workers
            .minByOption(_._2)
            .map(_._1)
            .flatMap(w =>
              stopwordsSet.map(s => {
                w ! Resource(p.fromCommand, s).toCommand
                pageCoordinator(updateCoordinator, workers + (w -> (workers(w) + 1)), stopwordsSet)
              })
            )
            .getOrElse(Behaviors.unhandled)
        case _ => Behaviors.unhandled
      }
    )

  private def closed(nextCoordinator: Option[ActorRef[Command]], workers: Map[ActorRef[Command], Int]): Behavior[Command] =
    Behaviors.receiveMessage {
      case Available(a) =>
        workers
          .get(a)
          .map(n =>
            if (n > 1) {
              closed(nextCoordinator, workers + (a -> (n - 1)))
            } else {
              a ! PoisonPill
              if (workers.size > 1) {
                closed(nextCoordinator, workers - a)
              } else {
                nextCoordinator.foreach(_ ! PoisonPill)
                Behaviors.stopped[Command]
              }
            }
          )
          .getOrElse {
            a ! PoisonPill
            Behaviors.same
          }
      case _ => Behaviors.unhandled
    }
}
