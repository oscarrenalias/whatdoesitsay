package net.renalias.wdis.backend.server

import net.renalias.wdis.common.config.Config
import net.renalias.wdis.common.logger.SimpleLogger
import net.renalias.wdis.common.messaging._
import net.renalias.wdis.common.messaging.Constants._

import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.remote.{RemoteClient, RemoteNode}
import se.scalablesolutions.akka.util.Logging
import Actor._

import net.renalias.wdis.frontend.server.FrontendServer
import net.liftweb.common.{Full, Failure}

class BackendActor extends Actor with Logging {
	def receive = {
		case NewScanRequest(jobId, fileName, lang) => {
			log.info("Received NewScanRequest message - jobId = " + jobId + ", fileName = " + fileName)

			val processor = new ScanRequestProcessor(fileName, lang)			
			val response = processor.process match {
				case Full(text) => ScanRequestCompleted(jobId, text)
				case Failure(msg, ex, _) => {
					log.error("ScanRequestProcessor returned an error:" + ex.toString)
					ScanRequestError(jobId, ex.toString)
				}
			}
			
			FrontendServer ! response
		}
		case Echo(msg) => self.reply("Backend Server - Echoing message: " + msg)
		case _ => log.error("BackEndActor received a message that it did not understand")
	}
}

object BackendTestClient extends SimpleLogger {
	def main(args: Array[String]) = {

		val response = BackendServer !! Echo("Hello, world")
		println("The response from the server was = " + response.getOrElse("no response"))
	}
}

object BackendServer extends SimpleLogger {
	def start = {
		RemoteNode.start(host, port)
		RemoteNode.register(REQUEST_SERVICE_NAME, actorOf[BackendActor])

		log.info("Starting backend server: host = " + host + ", port = " + port)
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