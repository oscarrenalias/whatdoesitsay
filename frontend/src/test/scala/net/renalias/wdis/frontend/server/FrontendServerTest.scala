import net.renalias.wdis.common.config.ComponentRegistry
import net.renalias.wdis.common.messaging.Echo
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class FrontendServerTest extends FunSuite with ShouldMatchers {

	val frontendServer = ComponentRegistry.frontendServer
	
	test("It is possible to send messages to the frontend server") {
		frontendServer.start
		// send a message to the frontend actor
		val result = frontendServer !! Echo("Hello, world")
		result.getOrElse("failure") should not equal "failure"
	}
}