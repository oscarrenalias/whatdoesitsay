package net.renalias.frontend.mongo

import net.liftweb.util.Props
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.{Mongo, MongoOptions, ServerAddress}

object MongoConfig {
	def init {
		val srvr = new ServerAddress(Props.get("mongodb.host", "localhost"), Props.getInt("mongodb.port", 27017))
		val mo = new MongoOptions
		MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr, mo), Props.get("mongodb.dbname", "test"))
	}
}