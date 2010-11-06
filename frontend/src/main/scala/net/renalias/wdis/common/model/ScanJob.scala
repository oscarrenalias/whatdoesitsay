package net.renalias.wdis.frontend.model

import net.liftweb.couchdb._
import net.liftweb.record.field._
import net.liftweb.common._
import java.util.Calendar
import net.renalias.wdis.common.config.Config

object ScanJobLang extends Enumeration {
	val ENG, FIN, SPA, FRA, DEU = Value
}

object ScanJobStatus extends Enumeration {
	type ScanJobStatus = Value 
	val New, InProgress, Ready, Completed, Error = Value
}

class ScanJob extends CouchRecord[ScanJob] {
  def meta = ScanJob

	object description extends OptionalStringField(this, 250)
	object originalFileName extends StringField(this, 250)
	object internalFileName extends StringField(this, 250)
	object lang extends EnumField(this, ScanJobLang)
	object status extends EnumField(this, ScanJobStatus)
	object createdDate extends DateTimeField(this, Calendar.getInstance)
	object completedDate extends OptionalDateTimeField(this)
	object text extends OptionalTextareaField(this, 999999) // TODO: check if we can have a field of unlimited size here

	def internalFilePath = Config.getString_!("folders.incoming") + "/" + internalFileName.value
 }

object ScanJob extends ScanJob with CouchMetaRecord[ScanJob] {
  //def createRecord = new ScanJob

  def findAll: List[ScanJob] = {
    val viewReturn = ScanJob.queryView("scanjob", "scanjob_findAll")
    viewReturn match {
      case Full(v) =>  return v.toList
      case Empty => return Nil
    }
  }
}