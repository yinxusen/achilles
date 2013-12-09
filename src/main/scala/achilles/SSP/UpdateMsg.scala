/**
 *  Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package achilles.SSP

import akka.actor.Actor

trait UpdateMsg

case class QueryRecom(uid: String, content: String, loc: String) extends UpdateMsg
case class checkDB() extends UpdateMsg

trait RecResult

case class BasicRecResult(uid: String, rec: String, rank: Int) extends RecResult
