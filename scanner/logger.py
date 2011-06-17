import logging

# 
# provides common logging functionality
#
class Logger:
	def __init__(self, handler):
		self.log = logging.getLogger(handler)
		self.log.setLevel(logging.DEBUG)
		
	def debug(self, msg): self.log.debug(msg)
		
	def info(self, msg): self.log.info(msg)
	
	def error(self, msg): self.log.error(msg)
	
	def warn(self, msg): self.log.warn(msg)