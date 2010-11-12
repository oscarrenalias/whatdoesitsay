package net.renalias.wdis.backend

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite

import net.renalias.wdis.common.messaging._
import net.renalias.wdis.common.config.ComponentRegistry


class BackendServerTest extends FunSuite with ShouldMatchers {

	val backendServer = ComponentRegistry.backendServer

	test("The backend server correctly reports errors") (pending)

	test("The Echo service in the backend server responds as expected") {
		val response = backendServer !! Echo("Hello, world")
		response should equal(Some("Echo: Hello, world"))
	}

	test("Synchronous requests are correctly processed") {
		val response = backendServer !! NewSyncScanRequest("whatever")
		println(response)
	}
}