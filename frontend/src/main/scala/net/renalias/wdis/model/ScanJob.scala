package net.renalias.wdis.model

import net.liftweb.mapper._

object ScanJobStatus extends Enumeration {
	val New, InProgress, Ready, Translated, Error = Value
}

object ScanJobLang extends Enumeration {
	val ENG, FIN, SPA, FRA, DEU = Value
}

class ScanJob extends LongKeyedMapper[ScanJob] with IdPK {
	def getSingleton = ScanJob
	
	object description extends MappedPoliteString(this, 250)
	object originalFileName extends MappedString(this, 256)
	object internalFileName extends MappedString(this, 256)
	object lang extends MappedEnum(this, ScanJobLang)
	object status extends MappedEnum(this, ScanJobStatus)
	object created extends MappedDateTime(this)
	object text extends MappedText(this)
}

object ScanJob extends ScanJob with LongKeyedMetaMapper[ScanJob] {
	
}