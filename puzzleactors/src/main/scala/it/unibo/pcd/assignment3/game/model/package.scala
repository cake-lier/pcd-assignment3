package it.unibo.pcd.assignment3.game

import akka.actor.typed.ActorRef
import it.unibo.pcd.assignment3.game.controller.Message

package object model {
  type Card = Int
  type Move = Int
  type Actor = ActorRef[Message]
}
