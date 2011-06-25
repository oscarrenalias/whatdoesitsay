package net.renalias.frontend.comet

import net.liftweb.http.CometActor
import _root_.net.liftweb.util.Helpers._
import _root_.scala.xml.{Text}
import net.renalias.frontend.model.{OCRServiceResponse, OCRServiceRequest, ScanJobStatus, ScanJob}
import net.liftweb.common._

case class NewScanRequest(val jobId: String, val fileName: String)

case class UpdateScanRequest(val jobId: String, val scanJob: Box[OCRServiceResponse])

class ScanJobActor extends CometActor with Logger {

  override def defaultPrefix = Box(Some("ScanJob"))

  // full re-render every time
  override def devMode = true

  // to track whether the job has been completed or not
  var jobComplete = false

  // lifespan of the comet actor
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
        info("Job " + jobId + "isn't ready yet, setting up the actor...")
        Text("job: " + job.id.is + " - status: " + job.status.is)
      }
      case Full(job) if (job.status.is == ScanJobStatus.Completed) => {
        //log.debug("Job " + jobId + "is already completed, not need to set up the actor")
        //Text("job: " + job.jobId.is + " - status: " + job.status.is + "-text:" + job.text.is)
        info("Job compelted - returning full template")
          <lift:embed what="/templates-hidden/job-data"/>
      }
      case _ => errorNotFound
    }
  }

  override def localSetup = {
    info("Starting comet actor: " + {
      jobId
    })
    //CometActorManager ! AddJobListener(jobId, this)
  }

  override def localShutdown = {
    info("Shutting down comet actor: " + {
      jobId
    })
    //CometActorManager ! RemoveJobListener(jobId, this)
  }

  override def lowPriority = {
    case NewScanRequest(jobId, file) => {
      // send the request
      // TODO: what to do with the result? is it needed?
      debug("Calling OCR Service...")
      val result = OCRServiceRequest.call(file)
      debug("OCR Service result received: " + result.openOr("Response was a failure"))

      // update the record in the db
      this ! UpdateScanRequest(jobId, result)
      reRender(devMode)
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
      }
    }
    case _ => error("ScanJobActor got a message that did not understand")
  }
}