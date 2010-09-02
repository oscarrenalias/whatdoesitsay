package net.renalias.wdis.misc

import scala.actors._

trait ListenerManager {
	var listeners: List[Actor] = List()
	
	def addListener(listener: Actor) = listeners = listeners :+ listener
	def removeListener(listener: Actor) = listeners = listeners filterNot (_ == listener)
	def notify(message: Any) = listeners.foreach(_ ! message)
}