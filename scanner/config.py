#
# specific configruation
#
class Config:
	
	#
	# patterns of the files that are processed
	#
	filePatterns = [ "*.jpg", "*.png", "*.tif", "*.tiff", "*.jpeg" ]
	
	#
	# poll time, in seconds
	#
	pollTime = 1
	
	#
	# base folder
	#
	baseFolder = "../files/"
	
	#
	# folder to watch for incoming data
	#
	incoming = baseFolder + "incoming"
	processing = baseFolder + "processing"
	completed = baseFolder + "completed"
	error = baseFolder + "error"
	archive = baseFolder + "archive"
	
	#
	# path to tesseract
	#
	tesseract = "/usr/local/bin/tesseract"
	
	#
	# path to convert from the imagemagick tools
	#
	convert = "/usr/local/bin/convert"