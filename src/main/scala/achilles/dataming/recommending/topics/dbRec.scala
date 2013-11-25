package achilles.dataming.recommending.topics

import achilles.backend.services.{QueryRecom, BasicRecResult}
import akka.actor.{Actor, ActorLogging}

/**
 * Created with IntelliJ IDEA.
 * User: sen
 * Date: 11/25/13
 * Time: 1:02 PM
 * To change this template use File | Settings | File Templates.
 */
class dbRec extends Actor with ActorLogging {
  def receive = {
    case QueryRecom(uid, content, location) => queryRec(uid, content, location)
    case _ => log.info("I have no idea.")
  }
  def queryRec(uid: String, content:String, loc: String): List[BasicRecResult] = Nil
}
