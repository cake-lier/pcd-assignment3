package it.unibo.pcd.assignment3.game.controller

import akka.cluster.ClusterEvent.MemberEvent
import it.unibo.pcd.assignment3.game.model.{Actor, Board, Card}

sealed trait Message
// player -> player
final case class LockRequest(requiringPlayer: Actor, timestamp: Long) extends Message
final case class LockPermitted(player: Actor) extends Message
final case class GameUpdate(board: Board, timestamp: Long) extends Message
final case class DiscoverGameStatus(requiringPlayer: Actor) extends Message
final case class GameStatus(lastUpdate: GameUpdate, player: Actor) extends Message
final case class DontHaveGameToo(player: Actor) extends Message
// cluster -> player
final case class ParticipantsChange(event: MemberEvent) extends Message
// receptionist -> player
final case class OnlinePlayers(players: Set[Actor]) extends Message
// controller -> local player
final case class Move(card1: Card, card2: Card) extends Message
// local player -> controller
// controller -> view
final case class NewBoard(board: Board) extends Message
// controller -> view
final case class GameEnded() extends Message
// view -> controller
final case class SelectCard(card: Card) extends Message
// view -> controller
final case class Stop() extends Message
