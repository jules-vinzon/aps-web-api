package app.utils

import app.App
import app.App.Companion.dotenv
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class Breadcrumb {
    private val log = App.log

    private var interval: DateTime = DateTime()
        .toDateTime(DateTimeZone.forID("Asia/Manila"))

    private var id = getRandomString()

    fun log(msg: String): String {
        try {
            val currentTime = DateTime().toDateTime(DateTimeZone.forID("Asia/Manila"))
            val diffInMillis = currentTime.millis - interval.millis
            var messageLog = msg
            if (dotenv["LOG_ENV"] != "local") {
                messageLog = msg.replace("\n", "\r")
            }
            log.info { "${id}\t${diffInMillis} ms.\t\t${messageLog}" }
            interval = currentTime
            return id
        } catch (e: Throwable) {
            e.printStackTrace()
            return id
        }
    }

    private fun getRandomString(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..12)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
