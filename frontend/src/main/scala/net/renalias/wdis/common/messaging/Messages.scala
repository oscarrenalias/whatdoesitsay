package net.renalias.wdis.common.messaging

case class NewAsyncScanRequest(requestId:String)
case class NewSyncScanRequest(requestId: String)
case class ScanRequestCompleted(requestId:String, text:String)
case class ScanRequestError(requestId:String, message:String)
case class Echo(msg: String)

object Constants {
	val REQUEST_SERVICE_NAME = "scanjob:request"
	val RESPONSE_SERVICE_NAME = "scanjob:response"
}

