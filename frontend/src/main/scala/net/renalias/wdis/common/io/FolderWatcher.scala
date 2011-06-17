package net.renalias.wdis.common.io

import scala.actors._
import java.io.File
import net.renalias.wdis.frontend.misc.ListenerManager
import net.renalias.wdis.common.logger._
import net.renalias.wdis.common.config.Config
import net.liftweb.common.Logger

abstract class FilePatternMatcher(val pattern:String) {
	def accept(file:String): Boolean
}

/**
 * This is a very simplified verison of a pattern matcher that matches extensions
 */
case class ExtensionMatcher(override val pattern:String) extends FilePatternMatcher(pattern) {
	def accept(file: String) = file.endsWith(pattern)
}

case class Folder(val folder:String, val patterns:List[ExtensionMatcher]=List(ExtensionMatcher(""))) {
	lazy val dir = new File(folder);
	def toList = patterns.flatMap(pattern => dir.list.toList.filter(file => pattern.accept(file)))
}

object String2ExtensionMatcher {
	// this will allow us to use a string where we would actually require a ExtensionMatcher object
	implicit def string2ExtensionMatcher(pattern:String): ExtensionMatcher = new ExtensionMatcher(pattern)
	// and this one allows us to provide a list of strings and return a list of ExtensionMatcher objects
	implicit def list2ExtensionMatcher(patterns:List[String]): List[ExtensionMatcher] = patterns.flatMap(x=>List(new ExtensionMatcher(x)))
}

abstract class FolderWatcherEvent(val files: List[String])
case class FilesAdded(override val files: List[String]) extends FolderWatcherEvent(files)
case class FilesRemoved(override val files: List[String]) extends FolderWatcherEvent(files)

class FolderWatcher(val folder: Folder, val delay:Int = 5000) extends Actor with ListenerManager with Logger {
	var previous, now: List[String] = List()
	
	def act() {		
		previous = folder.toList
		while (true) {
			// current list of files
			now = folder.toList
			// compare the prevous and current lists, to see if there's any changes
			val added = now filterNot (previous contains)
			val deleted = previous filterNot (now contains)
			
			// debug information
			if(added.nonEmpty) debug("Added files: " + added.mkString("\n"))
			if(deleted.nonEmpty) debug("Deleted files: " + deleted.mkString("\n"))			
			
			// notify the listeners
			added.nonEmpty match {
				case true => notify(new FilesAdded(added))
				case _ => // ignore
			}
			deleted.nonEmpty match {
				case true => notify(new FilesRemoved(deleted))
				case _ => // ignore
			}
			
			// the current snapshot becomes the old snapshot
			previous = now
			
			Thread.sleep(delay)
		}
	}
}

// static folder watcher object - client classes should use this one
object FolderWatcher extends FolderWatcher(
	new Folder(Config.getString("folders.completed", ".")), 
	Integer.parseInt(Config.getString("watcher.frequency", "5000"))) 
	with Logger {
		info("FolderWatcher starting - folder = " + folder.toString + ", poll frequency = " + delay + "ms")
}