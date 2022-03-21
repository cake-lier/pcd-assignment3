package it.unibo.pcd.assignment3.actors.model.tasks

/** The types of "filter" [[Task]] which are present in this application. */
object FilterTaskType extends Enumeration {

  type FilterTaskType = Value

  /** The [[Task]] which transforms [[it.unibo.pcd.assignment3.actors.model.entities.FilePath]]s into
    * [[it.unibo.pcd.assignment3.actors.model.entities.Document]]s.
    */
  val Path: Value = Value

  /** The [[Task]] which transforms [[it.unibo.pcd.assignment3.actors.model.entities.Document]]s into
    * [[it.unibo.pcd.assignment3.actors.model.entities.Page]]s.
    */
  val Document: Value = Value

  /** The [[Task]] which transforms [[it.unibo.pcd.assignment3.actors.model.entities.Page]]s into
    * [[it.unibo.pcd.assignment3.actors.model.entities.Update]]s.
    */
  val Page: Value = Value
}
