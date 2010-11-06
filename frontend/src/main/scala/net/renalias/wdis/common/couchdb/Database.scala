package net.renalias.wdis.common.couchdb

import net.liftweb.couchdb._	
import dispatch.{Http, StatusCode}
import net.liftweb.common.{Failure, Full}
import net.liftweb.json.Implicits.{int2jvalue, string2jvalue}
import net.liftweb.json.JsonAST.{JField, JInt, JObject, JString, render}
import net.liftweb.json.JsonDSL.{jobject2assoc, pair2Assoc, pair2jvalue}


object Database {
	import CouchDB.defaultDatabase

	val design: JObject =
	("language" -> "javascript") ~
					("views" -> (("scanjobs_findAll" -> ("map" -> "function(doc) { if (doc.type == 'ScanJob'){emit(doc.owner, doc)};}"))))

	def setup = {
		val database = new Database("scanjobs")
		try {Http(database info)} catch {
			case StatusCode(404, _) => {
				Http(database create)
				Http(database.design("scanjobs") put design)
			}
		}
		defaultDatabase = database
	}
}