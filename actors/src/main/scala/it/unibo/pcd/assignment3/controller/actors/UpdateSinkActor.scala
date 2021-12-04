package it.unibo.pcd.assignment3.controller.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import it.unibo.pcd.assignment3.controller.actors.Command.{PoisonPill, Ready, TimerExpired, UpdateCommand}
import it.unibo.pcd.assignment3.model.entities.Update
import it.unibo.pcd.assignment3.view.View

import scala.collection.immutable.ListMap
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

object UpdateSinkActor {

  def apply(
    root: ActorRef[Command],
    wordsNumber: Int,
    view: View
  ): Behavior[Command] =
    Behaviors.setup { _ =>
      root ! Ready
      Behaviors.withTimers { s =>
        val timerKey: Int = 0
        s.startTimerAtFixedRate(timerKey, TimerExpired, FiniteDuration((1000.0 / 60.0).round, MILLISECONDS))
        receiveUpdate(s, timerKey, wordsNumber, view, Update(Map.empty[String, Long], 0), poisoned = false)
      }
    }

  private def receiveUpdate(
    timerScheduler: TimerScheduler[Command],
    timerKey: Int,
    wordsNumber: Int,
    view: View,
    cumulatedUpdate: Update,
    poisoned: Boolean
  ): Behavior[Command] =
    Behaviors.receiveMessage {
      case UpdateCommand(f, w) =>
        receiveUpdate(
          timerScheduler,
          timerKey,
          wordsNumber,
          view,
          Update(
            f.foldLeft(cumulatedUpdate.frequencies)((m, e) => m + (e._1 -> (m.getOrElse(e._1, 0L) + e._2))),
            cumulatedUpdate.processedWords + w
          ),
          poisoned
        )
      case PoisonPill => receiveUpdate(timerScheduler, timerKey, wordsNumber, view, cumulatedUpdate, poisoned = true)
      case TimerExpired =>
        if (cumulatedUpdate.processedWords > 0) {
          view.displayProgress(
            cumulatedUpdate
              .frequencies
              .toSeq
              .sorted(Ordering.by[(String, Long), Long](_._2).reverse.orElse(Ordering.by[(String, Long), String](_._1)))
              .take(wordsNumber)
              .to(ListMap.mapFactory[String, Long]),
            cumulatedUpdate.processedWords
          )
        }
        if (poisoned) {
          view.displayCompletion()
          timerScheduler.cancel(timerKey)
          Behaviors.stopped
        } else {
          Behaviors.same
        }
      case _ => Behaviors.unhandled
    }
}
