package net.renalias.wdis.frontend.server

import net.renalias.wdis.common.config.Config
import net.renalias.wdis.common.messaging._
import net.renalias.wdis.common.messaging.Constants._
import net.renalias.wdis.common.logger.SimpleLogger
import net.renalias.wdis.common.io._
import net.renalias.wdis.frontend.model._

import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.remote.{RemoteClient, RemoteNode}
import se.scalablesolutions.akka.util.Logging
import Actor._

import java.util.Date

import _root_.net.liftweb._
import mapper._
import _root_.net.liftweb.common.{Box,Full,Empty}

class FrontendActor extends Actor {
	def receive = {
		case ScanRequestCompleted(jobId, text) => {
			println("Received ScanRequestCompleted message - jobId = " + jobId + ", text = " + text)
			
			// update the job in the database
			ScanJob.find(By(ScanJob.jobId, jobId)) match {
				case Full(job) => {
					// get the contents of the text file and update the status
					job.status(ScanJobStatus.Completed).
					text(text).
					completedDate(new Date).
					save 
					// notify listeners
					ScanJobMonitor.notify(JobCompleted(jobId))
				}
				case _ => log.error("There was no matching job with id = " + jobId)
			}			
		}
		case ScanRequestError(jobId, errorText) => {
			log.debug("Received ScanRequestError message - jobId = " + jobId + ", errorText = " + errorText)			
		}
		case Echo(msg) => self.reply("FrontendServer - Echoing message: " + msg)
		case _ => log.error("FrontEndActor received a message that it did not understand")
  	}
}

object FrontendTestClient extends SimpleLogger {
	def main(args: Array[String]) = {
		val response = FrontendServer !! Echo("Hello, world")
		println("The response from the server was = " + response.getOrElse("no response"))
	}
}

object FrontendServer extends SimpleLogger {
	def start = {		
		RemoteNode.start(host, port)
		RemoteNode.register(RESPONSE_SERVICE_NAME, actorOf[FrontendActor])		
		
		log.info("Starting frontend server: host = " + host + ", port = " + port)
	}
	
	def actor = {
		RemoteClient.actorFor(RESPONSE_SERVICE_NAME, host, port)
	}
	
	lazy val port = Config.getInt("akka.frontend.port", 9999)
	lazy val host = Config.getString("akka.frontend.host", "localhost")	
	
	def !(msg: AnyRef) = actor ! msg
	def !!(msg: AnyRef) = actor !! msg
}