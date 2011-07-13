package net.renalias.frontend.mongo

import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.{Mongo, MongoOptions, ServerAddress}
import net.renalias.frontend.config.Config

object MongoConfig {
	def init {
		val srvr = new ServerAddress(Config.getString_!("mongodb.host"), Config.getInt("mongodb.port", 27017))
		val mo = new MongoOptions
		MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr, mo), Config.getString_!("mongodb.dbname"))
	}
}