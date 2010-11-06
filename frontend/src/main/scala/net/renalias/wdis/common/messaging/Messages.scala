package net.renalias.wdis.common.messaging

import net.renalias.wdis.frontend.model.ScanJob

object Constants {
	val REQUEST_SERVICE_NAME = "scanjob:request"
	val RESPONSE_SERVICE_NAME = "scanjob:response"
}

case class NewScanRequest(val requestId:String)
case class ScanRequestCompleted(val requestId:String, val text:String)
case class ScanRequestError(val requestId:String, val message:String)

// test messages
case class Echo(val msg: String)