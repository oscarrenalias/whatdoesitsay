import sbt._

class LiftProject(info: ProjectInfo) extends DefaultWebProject(info) with IdeaProject {
  val liftVersion = "2.4-M2"

  // uncomment the following if you want to use the snapshot repo
  //  val scalatoolsSnapshot = ScalaToolsSnapshots

  // If you're using JRebel for Lift development, uncomment
  // this line
  // override def scanDirectories = Nil

  override val jettyPort = 8081

  override def libraryDependencies = Set(
    "net.liftweb" %% "lift-util" % liftVersion % "compile" withSources(),
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile" withSources(),
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile" withSources(),
	  "net.liftweb" %% "lift-mongodb" % liftVersion withSources(),
	  "net.liftweb" %% "lift-mongodb-record" % liftVersion withSources(),
	  "net.liftweb" %% "lift-wizard" % liftVersion withSources(),
    "org.mortbay.jetty" % "jetty" % "6.1.22" % "compile",
    "org.mortbay.jetty" % "jetty" % "6.1.22" % "test",
    "junit" % "junit" % "4.5" % "test",
    "ch.qos.logback" % "logback-classic" % "0.9.26",
    "org.scala-tools.testing" %% "specs" % "1.6.8" % "test",
    "com.h2database" % "h2" % "1.2.138",
	"net.databinder" %% "dispatch-http" % "0.8.3",
	"net.databinder" %% "dispatch-nio" % "0.8.3"
  ) ++ super.libraryDependencies
}
