package net.renalias.wdis.common.logger

import net.lag.logging.Logger

trait SimpleLogger {	
	lazy val log = Logger.get
}