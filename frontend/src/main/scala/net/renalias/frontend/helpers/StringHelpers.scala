package net.renalias.frontend.helpers

import java.util.UUID

object StringHelpers {
	def randomString = UUID.randomUUID().toString();
}