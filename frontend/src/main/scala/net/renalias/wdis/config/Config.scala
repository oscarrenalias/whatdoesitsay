package net.renalias.wdis.config

import net.lag.configgy.Configgy

object Config {
	lazy val config = {
		Configgy.configure("settings.conf")		
		Configgy.config
	}
	
	def getString(key: String) = config.getString(key)
	def getString(key: String, default: String) = config.getString(key, default)
}