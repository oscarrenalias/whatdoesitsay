import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.remote.{RemoteClient, RemoteNode}
import Actor._

import net.renalias.wdis.frontend.server.FrontendServer
import net.renalias.wdis.common.config.Config
import net.renalias.wdis.common.messaging._
 
class FrontendServerTest extends FunSuite with ShouldMatchers {
	
	test("It is possible to send messages to the frontend server") {
		FrontendServer.start
		// send a message to the frontend actor
		val result = FrontendServer !! Echo("Hello, world")
		result.getOrElse("failure") should not equal "failure"
	}
}