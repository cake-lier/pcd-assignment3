package it.unibo.pcd.assignment3.model.tasks

/** The types of "filter" {@link it.unibo.pcd.assignment3.model.tasks.Task} which are allowed into this application.
  */
object FilterTaskType extends Enumeration {
  type FilterTaskType = Value

  val Path: Value = Value

  val Document: Value = Value

  val Page: Value = Value
}
