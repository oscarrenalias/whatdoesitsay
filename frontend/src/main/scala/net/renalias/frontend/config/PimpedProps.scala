package net.renalias.frontend.config

import net.liftweb.util.Props

object PimpedProps {
	def get_!(name:String) = Props.get(name).getOrElse({throw new Exception("Key: " + name + " not found")})
	def getf(name:String, f:(String)=>String) = f(PimpedProps.get_!(name))
}