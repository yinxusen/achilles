/**
 *  Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package achilles.backend.services

/*
 * comments like //#<tag> are there for inclusion into docs, please don’t remove
 */

import akka.kernel.Bootable
import akka.actor._
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import akka.pattern.ask
import akka.actor.Status._
import scala.concurrent.ExecutionContext.Implicits.global

//#actor
class MainServerActor(streamActor: ActorRef, dbActor: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case QueryRecom(uid, content, location) =>
      log.info("Query recommendation from Rec Actor")
      val streamFuture = streamActor.ask(QueryRecom(uid, content, location))(1 seconds)
      val dbFuture = dbActor.ask(QueryRecom(uid, content, location))(1 seconds)
      streamFuture onComplete {
        case Success(result) => sender ! result
        case Failure(result) => {
          dbFuture onComplete {
            case Success(result) => sender ! result
            case Failure(result) => Nil
              log.info("Time out both in streaming and database recommendation")
          }
        }
      }
  }
}
//#actor

class MainServerApp extends Bootable {
  //#setup
  val system = ActorSystem("MainServerApp",
    ConfigFactory.load.getConfig("mainserver"))
  val actor = system.actorOf(Props[MainServerActor], "MainServerActor")
  //#setup

  def startup() {
  }

  def shutdown() {
    system.shutdown()
  }
}

object RecApp {
  def main(args: Array[String]) {
    new MainServerApp
    println("Started Calculator Application - waiting for messages")
  }
}
