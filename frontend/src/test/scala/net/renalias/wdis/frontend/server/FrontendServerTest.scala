import net.renalias.wdis.common.messaging.Echo
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import net.renalias.wdis.frontend.server.FrontendServer

class FrontendServerTest extends FunSuite with ShouldMatchers {
	
	test("It is possible to send messages to the frontend server") {
		FrontendServer.start
		// send a message to the frontend actor
		val result = FrontendServer !! Echo("Hello, world")
		result.getOrElse("failure") should not equal "failure"
	}
}