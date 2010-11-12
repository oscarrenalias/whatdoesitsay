package net.renalias.wdis.backend.server

import net.renalias.wdis.common.messaging._
import net.liftweb.common.{Empty, Logger, Full, Failure}
import net.renalias.wdis.frontend.model.ScanJob
import net.renalias.wdis.common.server.AkkaActorServer

import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.actor.Actor._
import net.renalias.wdis.common.config.{ComponentRegistry, Config}

class BackendActor extends Actor with Logger {

	println("BackendActor initializing")

	def processScanJob(job: ScanJob) = {
		val processor = new ScanRequestProcessor(job)
		processor.process match {
			case Full(text) => ScanRequestCompleted(job.id.value.get, text)
			case Failure(msg, ex, _) => {
				error("ScanRequestProcessor returned an error:" + ex.toString)
				ScanRequestError(job.id.value.get, ex.toString)
			}
			case _ => ScanRequestError("-1", "Job could not be found") 
		}
	}

	def processJob(jobId: String) = {
		println("Processing job: " + jobId)
		val result = ScanJob.fetch(jobId) match {
			case Full(job) => println("Found!");processScanJob(job)
			case _ => {
				println("Not found!");
				ScanRequestError(jobId, "Job could not be found")
			}
		}
		result
	}

	def receive = {
		case NewAsyncScanRequest(jobId) => ComponentRegistry.frontendServer ! processJob(jobId)
		case NewSyncScanRequest(jobId) => self.reply(processJob(jobId))
		case Echo(msg) => self.reply("Echo: " + msg)
		case _ => self.reply(ScanRequestError("-1", "The back end server received a message that it did not understand"))
	}
}

object BackendTestClient extends Logger {
	def main(args: Array[String]) = {
		val response = ComponentRegistry.backendServer !! Echo("Hello, world")
		println("The response from the server was = " + response.getOrElse("no response"))
	}
}

trait BackendServerComponent {

	val backendServer: BackendServer

	class BackendServer extends AkkaActorServer {
		override lazy val port = Config.getInt("akka.backend.port", 9998)
		override lazy val host = Config.getString("akka.backend.host", "localhost")
		override lazy val serviceName = net.renalias.wdis.common.messaging.Constants.REQUEST_SERVICE_NAME
		override val actorRef = actorOf[BackendActor]
	}
}

object BootableBackendServer {
	def main(args : Array[String]) = {
		// since we're not going through Lift's Boot.scala, we need to initialize the CouchDB connection by ourselves
		ComponentRegistry.database.setup
		// and then we start the server
		ComponentRegistry.backendServer.start
	}

}

/**
 * Only required if the backend server is booted as part of Akka's microkernel
 */
class AkkaBackendServer extends AkkaActorServer {
	override lazy val port = Config.getInt("akka.backend.port", 9998)
	override lazy val host = Config.getString("akka.backend.host", "localhost")
	override lazy val serviceName = net.renalias.wdis.common.messaging.Constants.REQUEST_SERVICE_NAME
	override val actorRef = actorOf[BackendActor]
	
	ComponentRegistry.database.setup
	
	start
}