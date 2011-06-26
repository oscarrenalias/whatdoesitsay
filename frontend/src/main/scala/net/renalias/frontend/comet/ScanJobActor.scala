package net.renalias.frontend.comet

import net.liftweb.http.CometActor
import _root_.net.liftweb.util.Helpers._
import _root_.scala.xml.{Text}
import net.renalias.frontend.model._
import net.renalias.frontend.snippet.document._
import net.liftweb.common._
import actors.Actor

case class NewScanRequest(val jobId: String, val fileName: String)
case class UpdateScanRequest(val jobId: String, val scanJob: Box[OCRServiceResponse])
case class ScanRequestReady(val jobId: String, val scanJob: Box[OCRServiceResponse])

class ScanJobActor extends CometActor with Logger {

  override def defaultPrefix = Box(Some("ScanJob"))

  // full re-render every time
  override def devMode = true

  // to track whether the job has been completed or not
  var jobComplete = false

  // maximum lifespan of the comet actor
  override def lifespan = Full(5 minutes)

  lazy val errorNotFound = Text("Job information not found")

  // retrieves the job identifier
  def jobId = name match {
    case Full(x) => x
    case _ => error("Empty jobId for actor!"); ""
  }

  def render = {
    ScanJob.find(jobId) match {
      case Full(job) if (job.status.is != ScanJobStatus.Completed) => {
        debug("Job " + jobId + "isn't ready yet, setting up the actor...")
        Text("job: " + job.id.is + " - status: " + job.status.is)
      }
      case Full(job) if (job.status.is == ScanJobStatus.Completed) => {
        debug("Job completed - returning full template")
        <lift:embed what="/templates-hidden/job-data"/>
      }
      case _ => errorNotFound
    }
  }

  override def localSetup = debug("Starting comet actor: " + jobId )

  override def localShutdown = debug("Shutting down comet actor: " + jobId)

  override def lowPriority = {
    case NewScanRequest(jobId, file) => {
      // moved to a separate actor so that it does not block this actor
      val doRequest = new Actor {
        def act() {
          react {
            case (f:String,sender:CometActor) => {
              val result = OCRServiceRequest.call(f)
              debug("OCR Service result received: " + result.openOr("Response was a failure"))
	            // send a message back to the Comet actor
              sender !  UpdateScanRequest(jobId, result)
            }
          }
        }
      }
      doRequest.start
      doRequest ! (file, this)
    }
    case UpdateScanRequest(jobId, result) => {
      // update the record in the db
      ScanJob.find(jobId) map { job =>
        // this body will only run if the job was found in the db
        result match {
          case Full(response) => {
            job.text.set(response.text)
            job.status.set(ScanJobStatus.Completed)
            job.save
          }
          case _ /*Failure(msg, _, _)*/ => {
            job.status.set(ScanJobStatus.Error)
            job.save
          }
        }
        this ! ScanRequestReady(jobId, result)
      }
    }
    case ScanRequestReady(jobId, result) => {
      debug("Scan request:" + jobId + " is ready. Refreshing.")

      // update the request var so that the snippet can find it
      documentIdSessionVar(Full(jobId))
      // and refresh the page
      reRender(devMode)
    }
    case _ => error("ScanJobActor got a message that did not understand")
  }
}