package net.renalias.wdis.common.config

import net.renalias.wdis.backend.server.BackendServerComponent
import net.renalias.wdis.frontend.server.FrontendServerComponent
import net.renalias.wdis.common.couchdb.DatabaseComponent

/**
 * Basic form of DI using the Cake pattern. where this class takes care of the wiring
 */
object ComponentRegistry
			extends DatabaseComponent
			with FrontendServerComponent
			with BackendServerComponent {
	lazy val database = new Database
	lazy val frontendServer = new FrontendServer
	lazy val backendServer = new BackendServer
}