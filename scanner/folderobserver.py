from logger import Logger

# global logging class
l = Logger("FolderObserver")

#
# Implements basic functionality of a listener manager
#
class ListenerManager:
	
	listeners = []
	
	def __init__(self):
		self.listeners = []
		
	#
	# add a listener to the list
	#
	def addListener(self, listenerClass):
		self.listeners.append(listenerClass)
	
	#
	# notiify all the listeners of the given event
	#	
	def notify(self, event):
		for listener in self.listeners:
			listener.notify(event)
		
#
# Interface that should be implemented by all classes that intend
# to receive notifications for changes in a listener manager
#		
class Listener:
	
	def notify(self, event):
		raise NotImplementedError("Listener.notify() must be implemented in subsclasses")

#
# Base class for events. Encapsulates all possible information that may be sent in an event
#
class Event:
	
	name = ""
	params = None
	
	def __init__(self, event, params=None):
		self.name = event
		self.params = params		

#
# Specific class for notifying of folder changes
#
class FolderChangesEvent(Event):
	
	def __init__(self, event, params=None):
		Event.__init__(self, event, params)

#
# Observes the changes in a given folder and generates a notification if changes
#		
class FolderObserver(ListenerManager):
	
	# folder that is observed
	folder = ""		
	# poll time
	pollTime = 0
	# force rescan upon startup
	forceRescan = False
	# default file name pattern
	patterns = ["*"]
	# ignore case in file names
	ignoreCase = True
		
	# default polling time (in seconds)
	DEFAULT_POLL_TIME = 10
		
	def __init__(self, folder, pollTime = DEFAULT_POLL_TIME, forceRescan=False, patterns="*", ignoreCase=True):
		self.folder = folder		
		self.pollTime = pollTime			
		self.forceRescan = forceRescan
		self.patterns = patterns
		self.ignoreCase = ignoreCase
		
	def filter(self, files):
		import fnmatch
		matches = []
		for file in files:
			for pattern in self.patterns:
				# convert the pattern and the file name to lower case if we're ignoring the case
				if self.ignoreCase: 
					pattern = pattern.lower()
					file = file.lower()
				
				if fnmatch.fnmatch(file, pattern):
					matches.append(file)
				
		return matches
		
	def waitForChanges(self):
		import os, time
		
		l.info( "Waiting for changes..." )
		l.info( "  source folder = " + str(self.folder))
		l.info( "  pollTime = " + str(self.pollTime))
		l.info( "  pattern = " + str(self.patterns))
		
		before = dict ([(f, None) for f in self.filter(os.listdir(self.folder))])
		while 1:
		  time.sleep (self.pollTime)
		  after = dict ([(f, None) for f in self.filter(os.listdir(self.folder))])
		  added = [f for f in after if not f in before]
		  removed = [f for f in before if not f in after]
		  if added: 
			l.info("Added: " + ", ".join (added))
			event = FolderChangesEvent("FileAdded", added)
			self.notify(event)
		  if removed: 
			l.info("Removed: " + ", ".join (removed))			
			event = FolderChangesEvent("FileRemoved", removed)
			self.notify(event)
		  before = after