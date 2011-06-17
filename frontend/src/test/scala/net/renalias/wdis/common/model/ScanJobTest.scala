package net.renalias.wdis.common.model.ScanJobTest

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import net.renalias.wdis.frontend.model.{ScanJob, ScanJobStatus, ScanJobLang}
import net.renalias.wdis.common.config.ComponentRegistry

class ScanJobTest extends FunSuite with ShouldMatchers {

	def init = ComponentRegistry.database.setup

	test("Objects can be created to the CouchDB database and retrieved from there") {
			init

			var job = ScanJob.createRecord
			job.originalFileName.set("originalFileName")
			job.internalFileName.set("internalFileName")
			job.status.set(ScanJobStatus.New)
			job.lang.set(ScanJobLang.ENG)
			job.save

			job.saved_? should be(true)

			ScanJob.fetch(job.id.value.get).map(x => x.id.value.get should equal(job.id.value.get))
	}
}