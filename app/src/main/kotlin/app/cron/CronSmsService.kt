package app.cron

import app.App
import app.utils.Breadcrumb
import app.utils.SmsSender
import org.bson.Document
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.find
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class CronSmsService : Job {
    @Throws(JobExecutionException::class)
    override fun execute(context: JobExecutionContext) {
        val breadcrumb = Breadcrumb()
        breadcrumb.log("SCHEDULED CRON SMS SERVICE TRIGGERED")
        try {
            val parcel = getPendingParcels(breadcrumb)
            parcel.forEach {
                val currentDate = LocalDate.now()
                val cdtr = it.optString("concierge_datetime_received").substring(0, 10)
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                val finalDate = LocalDate.parse(cdtr, dateFormatter)
                val daysPassed = ChronoUnit.DAYS.between(finalDate, currentDate)

                breadcrumb.log("Parcel ID: ${it.optString("parcel_id")}")
                breadcrumb.log("DATE RECEIVED: $cdtr")
                breadcrumb.log("DATE NOW: $currentDate")
                breadcrumb.log("PERIOD: $daysPassed")

                if (daysPassed.toInt() == 96) {
                    updateParcelRecord(breadcrumb, it.optString("parcel_id") )
                }

                breadcrumb.log("SMS: ${SmsSender().sendSms(it, daysPassed.toString())}")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        breadcrumb.log("END OF CRON SMS SERVICE")
    }

    private fun getPendingParcels(breadcrumb: Breadcrumb): ArrayList<JSONObject> {
        breadcrumb.log("FETCH ON PARCEL TABLE")
        val parcelDataArray = ArrayList<JSONObject>()
        return try {
            val parcelData = App.mongoDatabase.getCollection("parcel")
            parcelData.find("{parcel_status:'PENDING',  concierge_name : {${MongoOperator.ne}: ''}}")
                .forEach {
                    parcelDataArray.add(JSONObject(it))
                }
            parcelDataArray
        } catch (e: Throwable) {
            e.printStackTrace()
            breadcrumb.log("NO DATA FETCHED")
            parcelDataArray
        }
    }

    private fun updateParcelRecord(breadcrumb: Breadcrumb, parcelId: String): Boolean {
        return try {
            breadcrumb.log("UPDATING ON PARCEL TABLE")
            val manilaTime = DateTimeZone.forID("Asia/Manila")
            val dt = DateTime(manilaTime).toString()
            val col = App.mongoDatabase.getCollection("parcel")
            val query = Document("parcel_status", "DISPOSED")
                .append("updated_at", dt)
            val filter = Document("parcel_id", parcelId)
            val updateFieldDoc = Document("\$set",query)
            col.updateOne(filter, updateFieldDoc).modifiedCount
            breadcrumb.log("UPDATED ON PARCEL TABLE")
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            breadcrumb.log("FAILED TO UPDATE ON PARCEL TABLE")
            false
        }
    }
}