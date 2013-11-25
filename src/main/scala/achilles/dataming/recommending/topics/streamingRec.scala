package achilles.dataming.recommending.topics

import achilles.backend.services.{QueryRecom, BasicRecResult}
import akka.actor.{ActorLogging, Actor}

/**
 * Created with IntelliJ IDEA.
 * User: sen
 * Date: 11/25/13
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */

class streamingRec extends Actor with ActorLogging {
  def receive = {
    case QueryRecom(uid, content, location) => sender ! queryRec(uid, content, location)
    case _ => log.info("I don't know what. Invalid message.")
  }

  def queryRec(uid: String, content: String, loc: String): List[BasicRecResult] = {
    Nil
  }
}
