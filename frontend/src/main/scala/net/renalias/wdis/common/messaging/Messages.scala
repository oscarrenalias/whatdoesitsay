package net.renalias.wdis.common.messaging

object Constants {
	val REQUEST_SERVICE_NAME = "scanjob:request"
	val RESPONSE_SERVICE_NAME = "scanjob:response"
}

case class NewScanRequest(val requestId:String, val file:String, val lang:String)
case class ScanRequestCompleted(val requestId:String, val text:String)
case class ScanRequestError(val requestId:String, val message:String)

// test messages
case class Echo(val msg: String)