package net.renalias.frontend.helpers

import org.mortbay.jetty.Server
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.webapp.WebAppContext

object ConsoleHelper {
	val server = new Server
	val scc = new SelectChannelConnector

	scc.setPort(8081)
	server.setConnectors(Array(scc))

	val context = new WebAppContext()
	context.setServer(server)
	context.setContextPath("/")
	context.setWar("src/main/webapp")

	server.addHandler(context)

	// starts the Jetty server from the console
	def start = server.start()
	// stops the Jetty server
	def stop {
		server.stop()
    server.join()
	}
}