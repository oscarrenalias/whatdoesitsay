package net.renalias.wdis.common.config

import net.lag.configgy.Configgy
import net.liftweb.common.Logger

object Config extends Logger {

	lazy val config = {
		Configgy.configure("settings.conf")
		Configgy.config
	}

	val logger = Logger("Config")

	class ConfigKeyNotFoundException(key:String) extends Exception("No value foound for key:" + key)
	
	def getString(key: String) = config.getString(key) match {
		case None => logger.warn("No configuration value found for key:" + key); None
		case Some(x) => Some(x)
	}

	/**
	 * Return the value for the given key, or throw an Exception if None
	 */
	def getString_!(key:String) = getString(key).getOrElse({throw new ConfigKeyNotFoundException(key); ""})
	
	def getString(key: String, default: String) = config.getString(key, default) match {
		case x:String if x==default => logger.warn("No configuration value found for key:" + key); x
		case x:String => x
	}
	
	def getInt(key:String, default: Int) = config.getInt(key, default) match {
		case x:Int if x==default => logger.warn("No configuration value found for key:" + key); x
		case x:Int => x
	}
}