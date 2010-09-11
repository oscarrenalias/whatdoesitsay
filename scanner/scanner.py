from logger import Logger
from folderobserver import Listener, FolderObserver, ThreadedFolderObserver
from config import Config
import shutil
import os
import subprocess
from threading import Thread

# global logging class
l = Logger("Scanner")

class FileUtils:
	@staticmethod
	def incoming(file):
		return Config.incoming + "/" + file

	@staticmethod
	def processing(file):
		return Config.processing + "/" + file		
		
	@staticmethod
	def completed(file):
		return Config.completed + "/" + file		
		
	@staticmethod
	def archive(file):
		return Config.archive + "/" + file

	@staticmethod
	def error(file):
		return Config.error + "/" + file		
		
	# checks if all the required folders are in place
	@staticmethod
	def checkFolders():
		return(
			os.path.isdir(Config.incoming) and
			os.path.isdir(Config.processing) and
			os.path.isdir(Config.completed) and
			os.path.isdir(Config.archive) and
			os.path.isdir(Config.error)
		)
		

#
# Listener class that reacts to folder changes
#		
class Scanner(Listener):
	
	# status constants
	STATUS_NEW = 1
	STATUS_IN_PROGRESS = 2
	STATUS_COMPLETED = 3
	STATUS_ERROR = 4
	
	def notify(self, event):
		print( "Event notified: " + event.name)
		if event.name != "FileAdded":
			# we are not really interested in this event...
			return

		# process all files
		for file in event.params:
			self.processFileFromEvent(file)
			
	def needsConversion(self, file):
		filename, ext = os.path.splitext(file)
		return (ext.lower() != ".tiff" and ext.lower() != ".tif")			
		
	def convert(self, file):
		filename, ext = os.path.splitext(file)
		newFile = filename + ".tif"
		print("Converting file: " + file)
		
		# execute the command
		subprocess.call([Config.convert, FileUtils.processing(file), FileUtils.processing(newFile)])
		
		return newFile
		
	def scan(self, file):
		# build the command line to execute tesseract
		print("Scaning file = %s" % (file))
		lang = "eng"
		fileName, ext = os.path.splitext(file)
		# tessearct adds ".txt" automatically
		output = fileName
		
		# call tesseract to perform the conversion
		subprocess.call([Config.tesseract, FileUtils.processing(file), FileUtils.completed(output), "-l", lang])		
		return True
		
	def setStatus(self, file, status):
		if status == self.STATUS_IN_PROGRESS:
			print("STATUS_IN_PROGRESS: Moving file %s to folder %s" % (file, Config.processing))
			shutil.move(FileUtils.incoming(file), FileUtils.processing(file))			
		if status == self.STATUS_COMPLETED:
			print("STATUS_COMPLETED: Archiving file %s to folder %s" % (file, Config.archive))
			# archive the image
			shutil.move(FileUtils.processing(file), FileUtils.archive(file))
		if status == self.STATUS_ERROR:
			print("STATUS_ERROR: Moving file %s to folder %s" % (file, Config.error))
			# move the image
			shutil.move(FileUtils.processing(file), FileUtils.error(file))			
			
	def processFileFromEvent(self, file):
		print("Processing file: "  + file)
		
		# move it to the processing folder
		self.setStatus(file, self.STATUS_IN_PROGRESS)		
		
		# does it need to be converted?
		if self.needsConversion(file):
			print("File %s needs to be converted" % (file))
			processFile = self.convert(file)
		else:
			print("File %s does not need to be converted" % (file))
			processFile = file
			
		# scan it and save the output to the outgoing folder		
		self.scan(processFile)
		
		# mark as completed
		self.setStatus(file, self.STATUS_COMPLETED)	
	
def main():
	# configure the most basic logging capabilities
	import logging
	logging.basicConfig()
	sl = Logger("Scanner")
	
	if FileUtils.checkFolders() == False:
		import sys
		print("One or more of the requird folders do not exist or are not valid. Please check the configuration file.")
		sys.exit(-1)
	
	while True:
		# create one listener
		scanner = Scanner()
		observer = ThreadedFolderObserver(
			Config.incoming, 
			pollTime=Config.pollTime, 
			patterns=Config.filePatterns, 
			ignoreCase=True
			)
	
		observer.addListener(scanner)
		observer.start()
		observer.join()
		# if the join() method terminates is because the thread ended (died through an exception, most likely)
		# Since we want to keep the processing running, we'll create a new one
		sl.error("ThreadedFolderObserver thread terminated, creating new one...")
	
main()