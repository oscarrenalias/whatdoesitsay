package net.renalias.wdis.backend

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite

import net.renalias.wdis.common.messaging._
import net.renalias.wdis.common.messaging.ScanRequestError
import server.BackendServer
import net.renalias.wdis.common.couchdb.Database
import se.scalablesolutions.akka.remote.RemoteClient


class BackendServerTest extends FunSuite with ShouldMatchers {

	test("The backend server correctly reports errors") (pending)

	test("The Echo service in the backend server responds as expected") {
		val response = BackendServer !! Echo("Hello, world")
		response should equal(Some("Echo: Hello, world"))
	}

	test("Synchronous requests are correctly processed") {
		val response = BackendServer !! NewSyncScanRequest("whatever")
		println(response)
	}
}