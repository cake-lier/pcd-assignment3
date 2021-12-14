package it.unibo.pcd.assignment3

import _root_.it.unibo.pcd.assignment3.Operations.discard
import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory
import it.unibo.pcd.assignment3.game.controller.{Controller, Message}

object Main {

  val clusterSystemName = "ClusterSystem"

  def main(args: Array[String]): Unit = {
    args match {
      case Array(a, p) => participate(a, p.toInt)
      case Array(p)    => create(p.toInt)
      case _           => create(25457)
    }
  }

  def participate(address: String, port: Int): Unit = {
    val config: String =
      s"""
         akka.cluster.seed-nodes=["akka://$clusterSystemName@$address:${port.toString}"]"""
    startup(config, create = false)
  }

  def create(canonicalPort: Int): Unit = {
    val config: String =
      s"""
         akka.remote.artery.canonical.port=${canonicalPort.toString}
         akka.cluster.seed-nodes=["akka://$clusterSystemName@127.0.0.1:${canonicalPort.toString}"]"""
    startup(config, create = true)
  }

  def startup(configString: String, create: Boolean): Unit = {
    val config = ConfigFactory.parseString(configString) withFallback ConfigFactory.load("application_cluster")
    discard {
      ActorSystem[Message](Controller(2, 3, create), clusterSystemName, config)
    }
  }
}
