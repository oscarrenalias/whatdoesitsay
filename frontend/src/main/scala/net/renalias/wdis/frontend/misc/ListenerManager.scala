package net.renalias.wdis.frontend.misc

import scala.actors._

/*trait Listener {
	// empty for now
	self: Actor =>
}*/

trait ListenerManager {
	
	type Listener = { def !(msg:Any); }	// as long as the object supports the ""!"" method, we don't really care what it is
	
	var listeners: List[Listener] = List()
	
	def addListener(listener: Listener) = listeners = listeners :+ listener
	def removeListener(listener: Listener) = listeners = listeners filterNot (_ == listener)
	def notify(message: Any) = listeners.foreach(_ ! message)
}