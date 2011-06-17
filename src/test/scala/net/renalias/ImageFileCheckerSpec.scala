package net.renalias

import imageio.ImageFileChecker
import org.specs.Specification

class ImageFileCheckerSpec extends Specification {

	"The ImageFileChecker class " should {
		"Return false if the file is .tif or .tiff" in {
			ImageFileChecker.isConversionNeeded("test.tif") mustEqual false
			ImageFileChecker.isConversionNeeded("test.tiff") mustEqual false
		}
		"Return true for all other file extensions" in {
			ImageFileChecker.isConversionNeeded("test.jpg") mustEqual true
			ImageFileChecker.isConversionNeeded("test.png") mustEqual true
		}
		"Return true for supported image types" in {
			ImageFileChecker.isSupported("test.jpg") mustEqual true
		}
		"Return false for unsupported image types" in {
			ImageFileChecker.isSupported("test.asdf") mustEqual false
		}
	}
}