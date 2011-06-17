package net.renalias.config

import net.lag.configgy.Configgy
import akka.event.slf4j.Logging

object Config extends Logging {

	lazy val config = {
		Configgy.configure("ocrservice.conf")
		// TODO: is there a way to get Configgy to correctly load resources from the classpath?
		//Configgy.configureFromResource("ocrservice.conf")
		Configgy.config
	}

	class ConfigKeyNotFoundException(key:String) extends Exception("No value foound for key:" + key)

	def getString(key: String) = config.getString(key).orElse({log.warn("No configuration value found for key:" + key); None})

	/**
	 * Return the value for the given key, or throw an Exception if None
	 */
	def getString_!(key:String) = getString(key).getOrElse({throw new ConfigKeyNotFoundException(key); ""})

	def getString(key: String, default: String) = config.getString(key, default)
	def getInt(key:String, default: Int) = config.getInt(key, default)
}