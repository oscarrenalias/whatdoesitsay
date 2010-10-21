package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, StandardDBVendor}
import _root_.java.sql.{Connection, DriverManager}

import net.renalias.wdis.frontend.model._
import net.renalias.wdis.common.io.ScanJobMonitor
import net.renalias.wdis.frontend.server.FrontendServer

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = 
	new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
			     Props.get("db.url") openOr 
			     "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
			     Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }	
	
    // where to search snippet
    LiftRules.addToPackages("net.renalias.wdis.frontend")

	// For mapper entities
    Schemifier.schemify(true, Schemifier.infoF _, ScanJob)

    // Build SiteMap
    val entries = Menu(Loc("Home", List("index"), "Home")) :: 
				  Menu(Loc("View Document", List("document"), "View Document")) :: Nil
    LiftRules.setSiteMap(SiteMap(entries:_*))

    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

	/**
	 * Our own routing for handling /document/<number> URLs
	 */
	LiftRules.rewrite.append {
		case RewriteRequest(ParsePath(List("document", documentNumber), _, _, _), _, _) => 
				RewriteResponse("document" :: Nil, Map("documentId" -> documentNumber ))
	}

    LiftRules.early.append(makeUtf8)

    S.addAround(DB.buildLoanWrapper)

	// start the folder watcher thread
	//FolderWatcher.start
	ScanJobMonitor.start
	
	FrontendServer.start
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}

