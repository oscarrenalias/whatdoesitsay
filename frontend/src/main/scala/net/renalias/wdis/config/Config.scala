package net.renalias.wdis.config

import net.lag.configgy.Configgy
import net.renalias.wdis.logger.SimpleLogger

object Config extends SimpleLogger {
	lazy val config = {
		Configgy.configure("settings.conf")
		Configgy.config
	}
	
	def getString(key: String) = config.getString(key) match {
		case None => log.warning("No configuration value found for key:" + key); None
		case Some(x) => Some(x)
	}
	
	def getString(key: String, default: String) = config.getString(key, default) match {
		case x:String if x==default => log.warning("No configuration value found for key:" + key); x
		case x:String => x
	}
}