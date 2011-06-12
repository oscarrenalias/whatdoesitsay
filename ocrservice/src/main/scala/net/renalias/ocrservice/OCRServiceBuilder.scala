package net.renalias.ocrservice

import cc.spray._
import net.renalias.ocrservice.OCRTypes._

trait OCRServiceBuilder extends ServiceBuilder {

	val scanPipeline: OCRPipelineType

	val service = {
		path("scan" / ".*".r) {
			id =>
				get {
					_.complete(scanPipeline(OCRRequest(id, "EN")).result.getOrElse("Nothing to show"))
				}
		}
	}
}