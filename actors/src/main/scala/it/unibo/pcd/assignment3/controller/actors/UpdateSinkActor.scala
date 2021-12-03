package it.unibo.pcd.assignment3.controller.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.controller.actors.Command.{Available, PoisonPill, TimerExpired, UpdateCommand}
import it.unibo.pcd.assignment3.model.entities.Update
import it.unibo.pcd.assignment3.view.View

import scala.collection.immutable.ListMap
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

object UpdateSinkActor {

  def apply(wordsNumber: Int, view: View, updateCoordinator: ActorRef[Command]): Behavior[Command] =
    apply(wordsNumber, view, updateCoordinator, Update(Map.empty[String, Long], 0), poisoned = false)

  private def apply(
    wordsNumber: Int,
    view: View,
    updateCoordinator: ActorRef[Command],
    cumulatedUpdate: Update,
    poisoned: Boolean
  ): Behavior[Command] =
    Behaviors.setup { c =>
      updateCoordinator ! Available(c.self)
      Behaviors.withTimers { s =>
        s.startTimerAtFixedRate(TimerExpired, FiniteDuration((1000.0 / 60.0).round, MILLISECONDS))
        Behaviors.receiveMessage {
          case UpdateCommand(f, w) =>
            apply(
              wordsNumber,
              view,
              updateCoordinator,
              Update(
                f.foldLeft(cumulatedUpdate.frequencies)((m, e) => m + (e._1 -> (m.getOrElse(e._1, 0L) + e._2))),
                cumulatedUpdate.processedWords + w
              ),
              poisoned
            )
          case PoisonPill => apply(wordsNumber, view, updateCoordinator, cumulatedUpdate, poisoned = true)
          case TimerExpired =>
            view.displayProgress(
              cumulatedUpdate
                .frequencies
                .toSeq
                .sorted(Ordering.by[(String, Long), Long](_._2).reverse.orElse(Ordering.by[(String, Long), String](_._1)))
                .take(wordsNumber)
                .to(ListMap.mapFactory[String, Long]),
              cumulatedUpdate.processedWords
            )
            if (poisoned) {
              view.displayCompletion()
              Behaviors.stopped
            } else {
              Behaviors.same
            }
          case _ => Behaviors.unhandled
        }
      }
    }
}
