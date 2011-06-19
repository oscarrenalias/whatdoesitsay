package bootstrap.liftweb

package bootstrap.liftweb

import net.liftweb._
import mongodb.{DefaultMongoIdentifier, MongoIdentifier, MongoDB}
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._
import mapper._
import com.mongodb.{MongoOptions, ServerAddress, Mongo}
import net.renalias.frontend.mongo.MongoConfig

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
    LiftRules.addToPackages("net.renalias.frontend")

	// For mapper entities - not needed if we're using couchdb
    //Schemifier.schemify(true, Schemifier.infoF _, ScanJob)

    // Build SiteMap
    val entries = Menu(Loc("Home", List("index"), "Home")) :: 
				  Menu(Loc("View Document", List("document"), "View Document")) :: Nil

    //LiftRules.setSiteMap(SiteMap(entries:_*))
    //def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    //LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

	/**
	 * Our own routing for handling /document/<number> URLs
	 */
	LiftRules.rewrite.append {
		case RewriteRequest(ParsePath(List("document", documentNumber), _, _, _), _, _) => 
				RewriteResponse("document" :: Nil, Map("documentId" -> documentNumber ))
	}

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    //LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    S.addAround(DB.buildLoanWrapper)

	  MongoConfig.init
  }
}