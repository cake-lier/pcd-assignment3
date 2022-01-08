package it.unibo.pcd.assignment3.actors.controller.actors

import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.actors.AnyOps.discard
import it.unibo.pcd.assignment3.actors.controller.actors.Command._
import it.unibo.pcd.assignment3.actors.model.entities._
import it.unibo.pcd.assignment3.actors.model.tasks.{DocumentFilterTask, FilterTaskType, PageFilterTask, PathFilterTask}
import it.unibo.pcd.assignment3.actors.view.View

import scala.concurrent.ExecutionContext

object RootActor {

  def apply(
    filesDirectory: FilePath,
    stopwordsFile: FilePath,
    wordsNumber: Int,
    view: View,
    executor: ExecutionContext,
    totalActors: Int
  ): Behavior[Command] =
    Behaviors.setup[Command] { c =>
      val updateSinkActor = c.spawn[Command](UpdateSinkActor(c.self, wordsNumber, view), name = "update_sink_actor")
      c.watch(updateSinkActor)
      val pageCoordinator: ActorRef[Command] =
        c.spawn[Command](CoordinatorActor.pageCoordinator(c.self, updateSinkActor), name = "page_coordinator")
      val documentCoordinator: ActorRef[Command] =
        c.spawn[Command](CoordinatorActor[DocumentCommand](c.self, pageCoordinator), name = "document_coordinator")
      val pathCoordinator: ActorRef[Command] =
        c.spawn[Command](CoordinatorActor[FilePathCommand](c.self, documentCoordinator), name = "path_coordinator")
      awaitCoordinators(
        spawnCount = 4,
        pathCoordinator,
        documentCoordinator,
        pageCoordinator,
        updateSinkActor,
        filesDirectory,
        stopwordsFile,
        wordsNumber,
        view,
        executor,
        totalActors - 4
      )
    }

  private def awaitCoordinators(
    spawnCount: Int,
    pathCoordinator: ActorRef[Command],
    documentCoordinator: ActorRef[Command],
    pageCoordinator: ActorRef[Command],
    updateSinkActor: ActorRef[Command],
    filesDirectory: FilePath,
    stopwordsFile: FilePath,
    wordsNumber: Int,
    view: View,
    executor: ExecutionContext,
    totalActors: Int
  ): Behavior[Command] =
    Behaviors.receive[Command] { (c, m) =>
      m match {
        case Ready if spawnCount > 1 =>
          awaitCoordinators(
            spawnCount - 1,
            pathCoordinator,
            documentCoordinator,
            pageCoordinator,
            updateSinkActor,
            filesDirectory,
            stopwordsFile,
            wordsNumber,
            view,
            executor,
            totalActors
          )
        case Ready =>
          val pageFilterFactory: Boolean => Behavior[Command] = f =>
            FilterTaskActor(
              c.self,
              pageCoordinator,
              updateSinkActor,
              PageFilterTask,
              executor,
              f
            )
          val documentFilterFactory: Boolean => Behavior[Command] = f =>
            FilterTaskActor[DocumentCommand, Document, Page, PageCommand](
              c.self,
              documentCoordinator,
              pageCoordinator,
              DocumentFilterTask,
              executor,
              pageFilterFactory,
              f
            )
          val pathFilterFactory: Boolean => Behavior[Command] = f =>
            FilterTaskActor[FilePathCommand, FilePath, Document, DocumentCommand](
              c.self,
              pathCoordinator,
              documentCoordinator,
              PathFilterTask,
              executor,
              documentFilterFactory,
              f
            )
          val workersToSpawn: LazyList[(FilterTaskType.Value, String)] =
            LazyList
              .continually(FilterTaskType.values.toSeq)
              .zipWithIndex
              .flatMap(e => e._1.map((_, e._2.toString)))
              .take(Math.max(totalActors - 2, 3))
          workersToSpawn.foreach {
            case (FilterTaskType.Path, n)     => c.spawn[Command](pathFilterFactory(true), name = s"path_filter_actor_$n")
            case (FilterTaskType.Document, n) => c.spawn[Command](documentFilterFactory(true), name = s"document_filter_actor_$n")
            case (FilterTaskType.Page, n)     => c.spawn[Command](pageFilterFactory(true), name = s"page_filter_actor_$n")
          }
          awaitWorkers(
            workersToSpawn.size,
            pathCoordinator,
            pageCoordinator,
            pathFilterFactory,
            updateSinkActor,
            filesDirectory,
            stopwordsFile,
            view,
            executor
          )
        case _ => Behaviors.unhandled
      }
    }

  private def awaitWorkers(
    spawnCount: Int,
    pathCoordinator: ActorRef[Command],
    pageCoordinator: ActorRef[Command],
    pathFilterFactory: Boolean => Behavior[Command],
    updateSinkActor: ActorRef[Command],
    filesDirectory: FilePath,
    stopwordsFile: FilePath,
    view: View,
    executor: ExecutionContext
  ): Behavior[Command] =
    Behaviors.receive { (c, m) =>
      m match {
        case Ready if spawnCount > 1 =>
          awaitWorkers(
            spawnCount - 1,
            pathCoordinator,
            pageCoordinator,
            pathFilterFactory,
            updateSinkActor,
            filesDirectory,
            stopwordsFile,
            view,
            executor
          )
        case Ready =>
          discard {
            c.spawn[Command](
              PathGeneratorActor(
                filesDirectory,
                stopwordsFile,
                e => view.displayError(e.getMessage),
                pathCoordinator,
                pageCoordinator,
                pathFilterFactory,
                executor
              ),
              name = "path_generator_actor"
            )
          }
          Behaviors.receiveSignal { case (_, Terminated(`updateSinkActor`)) => Behaviors.stopped }
        case _ => Behaviors.unhandled
      }
    }
}
