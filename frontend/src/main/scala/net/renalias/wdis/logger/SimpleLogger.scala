package net.renalias.wdis.logger

import net.lag.logging.Logger

trait SimpleLogger {	
	lazy val log = Logger.get
}