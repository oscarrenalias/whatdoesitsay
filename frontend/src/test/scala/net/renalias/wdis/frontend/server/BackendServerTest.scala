import java.util.Date
import net.renalias.wdis.frontend.model.{ScanJob, ScanJobLang, ScanJobStatus}
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

		/*val job = ScanJob.create.
							jobId("1").
							originalFileName("original-filename.jpg").
							internalFileName("internal-file-name.jpg").
							status(ScanJobStatus.New).
							lang(ScanJobLang.ENG).
							createdDate(new Date)

		BackendServer ! NewScanRequest(job)*/
	}
}