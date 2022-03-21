package it.unibo.pcd.assignment3.actors.controller.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import it.unibo.pcd.assignment3.actors.controller.actors.Command.{PoisonPill, Ready, TimerExpired, UpdateCommand}
import it.unibo.pcd.assignment3.actors.model.entities.Update
import it.unibo.pcd.assignment3.actors.view.View

import scala.collection.immutable.ListMap
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

/** The actor which is last in the data transformation chain, the one whose job is accumulate the [[Update]]s coming from the
  * PageFilterWorker actors, the workers of the PageCoordinator actor, and publish them to the user through the [[View]] component
  * at a fixed rate. This is the actor which, when shut down, causes the entire system to shut down itself, because it means that
  * no other [[Update]]s will be published, which follows from the fact that no other input is to be processed, meaning that the
  * computation is over.
  */
object UpdateSinkActor {

  /** Returns the behavior of an UpdateSink actor.
    * @param root
    *   the root actor of the system
    * @param wordsNumber
    *   the maximum number of words to be considered in an [[Update]] when presenting it to the user
    * @param view
    *   the [[View]] component
    * @return
    *   the behavior of an UpdateSink actor
    */
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
        main(s, timerKey, wordsNumber, view, Update(Map.empty[String, Long], 0), poisoned = false)
      }
    }

  /* The main state of a UpdateSink actor behavior. */
  private def main(
    timerScheduler: TimerScheduler[Command],
    timerKey: Int,
    wordsNumber: Int,
    view: View,
    cumulatedUpdate: Update,
    poisoned: Boolean
  ): Behavior[Command] =
    Behaviors.receiveMessage {
      case UpdateCommand(f, w) =>
        main(
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
      case PoisonPill => main(timerScheduler, timerKey, wordsNumber, view, cumulatedUpdate, poisoned = true)
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
