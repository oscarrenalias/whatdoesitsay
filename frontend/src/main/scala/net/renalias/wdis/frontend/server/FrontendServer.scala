package net.renalias.wdis.frontend.server

import net.renalias.wdis.common.messaging._
import net.renalias.wdis.common.messaging.Constants._
import net.renalias.wdis.frontend.model._
import net.renalias.wdis.common.server.AkkaActorServer

import se.scalablesolutions.akka.actor.Actor
import Actor._

import java.util.Calendar

import _root_.net.liftweb._
import common.{Logger, Box, Full, Empty}
import net.renalias.wdis.frontend.comet.JobCompleted
import net.renalias.wdis.common.config.{ComponentRegistry, Config}

class FrontendActor extends Actor with Logger {
	
	def receive = {
		case ScanRequestCompleted(jobId, text) => {
			info("Received ScanRequestCompleted message - jobId = " + jobId + ", text = " + text)
			
			ScanJob.fetch(jobId) match {
				case Full(job) => {
					// get the contents of the text file and update the status
					job.status(ScanJobStatus.Completed).
					text(text).
					completedDate(Calendar.getInstance).
					save 
					// notify listeners
					ComponentRegistry.cometActorManager.notify(JobCompleted(jobId))
				}
				case _ => log.error("There was no matching job with id = " + jobId)
			}			
		}
		case ScanRequestError(jobId, errorText) => {
			error("Received ScanRequestError message - jobId = " + jobId + ", errorText = " + errorText)
		}
		case Echo(msg) => self.reply("FrontendServer - Echoing message: " + msg)
		case _ => error("FrontEndActor received a message that it did not understand")
  }
}

object FrontendTestClient extends Logger {
	def main(args: Array[String]) = {
		val response = ComponentRegistry.frontendServer !! Echo("Hello, world")
		println("The response from the server was = " + response.getOrElse("no response"))
	}
}

trait FrontendServerComponent {

	val frontendServer: FrontendServer

	class FrontendServer extends AkkaActorServer {
		override lazy val port = Config.getInt("akka.frontend.port", 9999)
		override lazy val host = Config.getString("akka.frontend.host", "localhost")
		override lazy val serviceName = 	RESPONSE_SERVICE_NAME
		override val actorRef = actorOf[FrontendActor]
	}
}