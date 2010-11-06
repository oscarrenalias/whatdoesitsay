package net.renalias.wdis.backend.server

import net.renalias.wdis.common.config.Config
import net.renalias.wdis.common.messaging._
import net.renalias.wdis.common.messaging.Constants._

import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.remote.{RemoteClient, RemoteNode}
import Actor._

import net.renalias.wdis.frontend.server.FrontendServer
import net.renalias.wdis.frontend.model.ScanJob
import net.liftweb.common.{Logger, Full, Failure}

class BackendActor extends Actor with Logger {

	def processJob(job: ScanJob) = {
		val processor = new ScanRequestProcessor(job)
		processor.process match {
			case Full(text) => ScanRequestCompleted(job.id.value.get, text)
			case Failure(msg, ex, _) => {
				error("ScanRequestProcessor returned an error:" + ex.toString)
				ScanRequestError(job.id.value.get, ex.toString)
			}
		}
	}

	def receive = {
		case NewScanRequest(jobId) => {
			info("Received NewScanRequest message - jobId = " + jobId)
			val response = ScanJob.fetch(jobId).map(processJob(_))
			FrontendServer ! response
		}
		case Echo(msg) => self.reply("Backend Server - Echoing message: " + msg)
		case _ => error("BackEndActor received a message that it did not understand")
	}
}

object BackendTestClient extends Logger {
	def main(args: Array[String]) = {

		val response = BackendServer !! Echo("Hello, world")
		println("The response from the server was = " + response.getOrElse("no response"))
	}
}

object BackendServer extends Logger {
	def start = {
		RemoteNode.start(host, port)
		RemoteNode.register(REQUEST_SERVICE_NAME, actorOf[BackendActor])

		info("Starting backend server: host = " + host + ", port = " + port)
	}
	
	def actor = {
		RemoteClient.actorFor(net.renalias.wdis.common.messaging.Constants.REQUEST_SERVICE_NAME, host, port)		
	}
	
	lazy val port = Config.getInt("akka.backend.port", 9998)
	lazy val host = Config.getString("akka.backend.host", "localhost")

	def !(msg: AnyRef) = actor ! msg
	def !!(msg: AnyRef) = actor !! msg
	
	def main(args : Array[String]) = start
}