package net.renalias.wdis.common.config

import net.renalias.wdis.backend.server.BackendServerComponent
import net.renalias.wdis.frontend.server.FrontendServerComponent
import net.renalias.wdis.common.couchdb.DatabaseComponent
import net.renalias.wdis.frontend.comet.CometActorManagerComponent
import dispatch.{StatusCode, Http}
import net.liftweb.couchdb.{Database=>CouchDBDatabase, _}
import net.renalias.wdis.common.couchdb.DatabaseComponent

/**
 * Basic form of DI using the Cake pattern. where this class takes care of the wiring
 */
trait ComponentRegistry
			extends DatabaseComponent
			with FrontendServerComponent
			with BackendServerComponent
		  with CometActorManagerComponent {
	lazy val database = new Database
	lazy val frontendServer = new FrontendServer
	lazy val backendServer = new BackendServer
	lazy val cometActorManager = CometActorManager
}

object ComponentRegistry extends ComponentRegistry

object TestComponentRegistry extends ComponentRegistry with MockDatabaseCompoent {
	override lazy val database = new MockDatabase
}

trait MockDatabaseCompoent extends DatabaseComponent {
		val database: MockDatabase
		class MockDatabase extends Database {
			override def setup = println("**** SETTING UP THE DATABASE, THAT'S ALL ******")
		}
}

object Settings {
	implicit val registry = TestComponentRegistry 
}