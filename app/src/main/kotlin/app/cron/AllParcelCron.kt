package app.cron

import app.App
import app.services.parcel_logger.parcel.FetchParcelServices
import app.services.parcel_logger.pickUp.FetchPickUp
import app.utils.Breadcrumb
import org.litote.kmongo.MongoOperator
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import java.time.ZonedDateTime
import java.util.*

class AllParcelCron  : Job {
    private val fetch = FetchParcelServices()
    private val pickUp = FetchPickUp()
    @Throws(JobExecutionException::class)
    override fun execute(context: JobExecutionContext) {
        val breadcrumb = Breadcrumb()
        breadcrumb.log("SCHEDULED ALL PARCEL SERVICE TRIGGERED")
        try {
            App.dateStart = ZonedDateTime.now(App.zone).minusDays(95)
            App.dateEnd = ZonedDateTime.now(App.zone).plusDays(1)
            breadcrumb.log("DATE START ${App.dateStart}")
            breadcrumb.log("DATE END ${App.dateEnd}")
            val collection = App.mongoDatabase.getCollection("parcel")
            val count = collection.countDocuments()
            breadcrumb.log("COUNT OF PARCEL $count")
        } catch (e: Exception) {
            println("Error accessing the database: ${e.message}")
        } finally {
            App.allParcel = fetch.fetchBulkData(breadcrumb, "{concierge_name: { ${MongoOperator.ne}: '' },  created_at:{${MongoOperator.gte}: '${App.dateStart}', ${MongoOperator.lte}: '$App.dateEnd'}}")
            val query = "{created_at:{${MongoOperator.gte}: '${App.dateStart}', ${MongoOperator.lte}: '${App.dateEnd}'}}"
            App.allPickUp = pickUp.fetchBulkData(breadcrumb, query)
        }
    }
}