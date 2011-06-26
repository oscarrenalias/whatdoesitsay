package net.renalias.frontend.mongo

import net.liftweb.util.Props
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.{Mongo, MongoOptions, ServerAddress}
import net.renalias.frontend.config.PimpedProps

object MongoConfig {
	def init {
		val srvr = new ServerAddress(PimpedProps.get_!("mongodb.host"), Props.getInt("mongodb.port", 27017))
		val mo = new MongoOptions
		MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr, mo), PimpedProps.get_!("mongodb.dbname"))
	}
}