package net.renalias.frontend.model

import java.util.Calendar
import net.renalias.frontend.config.Config
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.record.field._

object ScanJobLang extends Enumeration {
	val ENG, FIN, SPA, FRA, DEU = Value
}

object ScanJobStatus extends Enumeration {
	type ScanJobStatus = Value 
	val New, InProgress, Ready, Completed, Error = Value
}

class ScanJob extends MongoRecord[ScanJob] with ObjectIdPk[ScanJob] {
  def meta = ScanJob

	object description extends OptionalStringField(this, 250)
	object originalFileName extends StringField(this, 250)
	object internalFileName extends StringField(this, 250)
	object lang extends EnumField(this, ScanJobLang)
	object status extends EnumField(this, ScanJobStatus)
	object createdDate extends DateTimeField(this, Calendar.getInstance)
	object completedDate extends OptionalDateTimeField(this)
	object text extends OptionalTextareaField(this, 999999) // TODO: check if we can have a field of unlimited size here

	def internalFilePath = Config.getStringF("folders.incoming", {r => r + "/" + internalFileName.value})
 }

object ScanJob extends ScanJob with MongoMetaRecord[ScanJob] {
	override def collectionName = "scanjobs"
}