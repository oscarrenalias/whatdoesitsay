import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.remote.{RemoteClient, RemoteNode}
import Actor._

import net.renalias.wdis.backend.server.BackendServer
import net.renalias.wdis.common.config.Config
import net.renalias.wdis.common.messaging._
 
class BackendServerTest extends FunSuite with ShouldMatchers {
	
	test("It is possible to send messages to the backend server") {
		BackendServer.start
		
		// send a message to the backend server
		val response = BackendServer !! Echo("Hello, world")
		
		response.getOrElse("failure") should not equal "failure"
	}
	
	test("The server is able to generate errors correctly") {
		// send a message to the backend server
		BackendServer ! NewScanRequest("jobId", "fileName", "lang")
	}
}