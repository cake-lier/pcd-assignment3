package it.unibo.pcd.assignment3.actors.model.tasks

/** The types of "filter" {@link it.unibo.pcd.assignment3.actors.model.tasks.Task} which are allowed into this application.
  */
object FilterTaskType extends Enumeration {
  type FilterTaskType = Value

  val Path: Value = Value

  val Document: Value = Value

  val Page: Value = Value
}
