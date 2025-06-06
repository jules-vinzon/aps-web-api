package app/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */

import app.cron.CronSms
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import io.github.cdimascio.dotenv.dotenv
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.litote.kmongo.KMongo
import app.database.connection.NodeConnection
import app.database.connection.NodeConnection.nodeDataSource
import app.routes.HealthCheck
import app.routes.aps.MainRoutes
import app.routes.parcel.*
import kotlinx.coroutines.*
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.cookie.StandardCookieSpec
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.eclipse.paho.client.mqttv3.*
import org.http4k.client.ApacheClient
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.ArrayList

class App {
    companion object {
        // Import Kotlin Logger
        val log = KotlinLogging.logger {}
        var allParcel = ArrayList<JSONObject>()
        var allPickUp = ArrayList<JSONObject>()
        val zone = ZoneId.of("Asia/Manila")
        var dateStart: ZonedDateTime = ZonedDateTime.now(zone)
        var dateEnd: ZonedDateTime = ZonedDateTime.now(zone)

        // Dotenv Config
        val dotenv = dotenv {
            ignoreIfMalformed = true
            ignoreIfMissing = true
        }

        /*
         * Javalin
         */
        val javalin: Javalin = Javalin.create { config ->
            config.maxRequestSize = 122880L
            config.enableCorsForAllOrigins()
            config.requestLogger { ctx, timeMs ->
                var breadcrumbId = ""
                try {
                    breadcrumbId = ctx.res.getHeaders("Breadcrumb").first()
                } catch (e: Throwable) {
                    // For request that has no breadcrumb
                }
                log.info {
                    breadcrumbId + " " + ctx.method() + " " + ctx.path() + " took " + timeMs + " ms " +
                            DateTime()
                                .toDateTime(DateTimeZone.forID("Asia/Manila"))
                                .toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
                }
            }
        }.start(dotenv["PORT"]!!.toInt())

        val httpClient = ApacheClient(
            client = HttpClients.custom().setDefaultRequestConfig(
                RequestConfig.custom()
                    .setRedirectsEnabled(true)
                    .setCookieSpec(StandardCookieSpec.RELAXED)
                    .build())
                .build()
        )

        private fun initializeJavalin() {
            // Added groupings of versions for v1 and v2 for sample purposes
            javalin.routes {
                ApiBuilder.path("/v1") {
                    ApiBuilder.path(dotenv["APP_URL_PREFIX"].toString()) {
                        // List of Routes on routes directory
                        HealthCheck().start()
                        ParcelRoutes().start()
                        SmsRoutes().start()
                        PickUpRoutes().start()
                        MainRoutes().start()
                    }
                }
                ApiBuilder.path("/v2") {
                    ApiBuilder.path(dotenv["APP_URL_PREFIX"].toString()) {
                        // List of Routes on routes directory
                        HealthCheck().start()
                    }
                }
            }
        }

        /*
        * Database
        */
        lateinit var dbNode: Database
        private fun initializeDatabase() {
            NodeConnection.init()
            dbNode = Database.connect(nodeDataSource)
        }

        /*
        * Mongo
        */
        lateinit var mongoClient: MongoClient
        lateinit var mongoDatabase: MongoDatabase
        private fun initializeMongo() {
            mongoClient = KMongo.createClient(dotenv["MONGO_CONNECTION_STRING"].toString())
            mongoDatabase = mongoClient.getDatabase(dotenv["MONGO_DATABASE_NAME"].toString())
        }

        var parcelNo = "W-000000"
        var pickupNo = "W-000000"
        private fun resetParcelNo() = runBlocking {
            val intervalDays = 100
            var lastResetDate = LocalDate.now()
            launch(Dispatchers.Default) {
                while (true) {
                    val currentDate = LocalDate.now()
                    val daysPassed = ChronoUnit.DAYS.between(lastResetDate, currentDate)
                    println("INTERVAL $daysPassed")
                    println("LAST RESET DATE $lastResetDate")
                    println("PARCEL NO $parcelNo")
                    if (daysPassed >= intervalDays) {
                        parcelNo = "W-000000"
                        pickupNo = "W-000000"
                        lastResetDate = currentDate
                        println("RESET DATE $lastResetDate")
                        println("PARCEL NO $parcelNo")
                    }

                    delay(24 * 60 * 60 * 1000) // Delay for 24 hours
                }
            }
        }

        lateinit var mqttClient: MqttClient
        private fun mqttConnect() {
            val brokerUrl = dotenv["BROKER_URL"].toString()
            val clientId = dotenv["CLIENT_ID"].toString()
            try {
                mqttClient = MqttClient(brokerUrl, clientId)
                val connOpts = MqttConnectOptions()
                connOpts.isCleanSession = true
                mqttClient.connect(connOpts)
                println("Connected to the broker")
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        /*
        * Main Fn
        */
        @JvmStatic
        fun main(args: Array<String>) {
//            initializeDatabase()
            mqttConnect()
            initializeJavalin()
            initializeMongo()
            CronSms().initializeCron()
            resetParcelNo()
        }
    }
}
