/**
 *  Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package achilles.backend.services

import akka.actor.Actor

trait RequireMsg

case class QueryRecom(uid: String, content: String, loc: String) extends RequireMsg

trait RecResult

case class RecFromStreaming(uid: String, rec: String, rank: Int) extends RecResult
