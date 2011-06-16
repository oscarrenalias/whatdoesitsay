package net.renalias.ocrservice

import cc.spray._
import net.renalias.ocrservice.OCRTypes._
import net.renalias.config.Config

trait OCRServiceBuilder extends ServiceBuilder {

	val scanPipeline: OCRPipelineType

	val service = {
		path("scan" / ".*".r) {
			id =>
				get {
					val incomingPath = Config.getString_!("folders.incoming")
					_.complete(scanPipeline(OCRRequest(incomingPath + id, "eng")).result.getOrElse("Nothing to show"))
				}
		}
	}
}