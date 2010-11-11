package net.renalias.wdis.common.server

import net.liftweb.common.Logger
import se.scalablesolutions.akka.remote.{RemoteNode, RemoteClient, RemoteServer}
import se.scalablesolutions.akka.actor.ActorRef

/**
 * Abstract trait that implements the Akka server functionalities
 *
 * Implement this trait in your class and override the following attributes:
 *
 * host
 * port
 * serviceName
 * actorRef
 */
trait AkkaActorServer extends Logger {

	lazy val port = 9999
	lazy val host = "localhost"
	lazy val serviceName = "AkkaActorServer"
	val actorRef: ActorRef

	def actor = {
		RemoteClient.actorFor(serviceName, host, port)
	}

	def !(msg: AnyRef) = actor ! msg
	def !!(msg: AnyRef) = actor !! msg
	def !!(msg: AnyRef, timeout: Int) = actor !! (msg, timeout)	

	def start = {
		val server = new RemoteServer
		server.start(host, port)
		server.register(serviceName, actorRef)

		info("AkkaActorServer starting: host = " + host + ", port = " + port)
	}
}