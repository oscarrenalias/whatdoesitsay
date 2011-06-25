package bootstrap.liftweb

import net.liftweb._
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
class Boot extends LazyLoggable {
  def boot {

    // where to search snippet
    LiftRules.addToPackages("net.renalias.frontend")

    // Build SiteMap
    def sitemap = SiteMap(
	        Menu(Loc("Home", List("index"), "Home")),
				  Menu(Loc("View Document", List("document"), "View Document")))

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemap)

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
	LiftRules.statefulRewrite.append {
		case RewriteRequest(ParsePath(List("document", documentNumber), _, _, _), _, _) => 
				RewriteResponse("document" :: Nil, Map("documentId" -> documentNumber ))
	}

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    //LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

 	  MongoConfig.init
  }
}