package net.renalias.config

import net.lag.configgy.Configgy
import akka.util.Logging

object Config extends Logging {

	lazy val config = {
		//Configgy.configure("ocrservice.conf")
		Configgy.configureFromResource("ocrservice.conf")
		Configgy.config
	}

	class ConfigKeyNotFoundException(key:String) extends Exception("No value foound for key:" + key)
	
	def getString(key: String) = config.getString(key) match {
		case None => log.warn("No configuration value found for key:" + key); None
		case Some(x) => Some(x)
	}

	/**
	 * Return the value for the given key, or throw an Exception if None
	 */
	def getString_!(key:String) = getString(key).getOrElse({throw new ConfigKeyNotFoundException(key); ""})

	// TODO: can this be optimized?
	def getString(key: String, default: String) = config.getString(key, default) match {
		case x:String if x==default => log.warn("No configuration value found for key:" + key); x
		case x:String => x
	}

	// TODO: can this be optimized?
	def getInt(key:String, default: Int) = config.getInt(key, default) match {
		case x:Int if x==default => log.warn("No configuration value found for key:" + key); x
		case x:Int => x
	}
}