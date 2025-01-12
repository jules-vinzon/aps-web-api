package app.cron

import app.App
import app.services.parcel_logger.parcel.FetchParcelServices
import app.services.parcel_logger.pickUp.FetchPickUp
import app.utils.Breadcrumb
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.litote.kmongo.MongoOperator
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class CronSms {
    private val schedFact: SchedulerFactory = StdSchedulerFactory()
    private val scheduler = schedFact.scheduler
    private val groupName = "sms_cron"
    private val breadcrumb = Breadcrumb()
    private val fetch = FetchParcelServices()
    private val pickUp = FetchPickUp()

    fun initializeCron() = runBlocking {
        breadcrumb.log("SPLIT PAYMENT CRON")
        scheduler.start()
        launch {
            fetchParcel()
        }

        for (group in scheduler.jobGroupNames) {
            for (jobKey in scheduler.getJobKeys(GroupMatcher.groupEquals(group))) {
                breadcrumb.log("CHECK JOB BEFORE RUNNING: $jobKey")
            }
        }

        val schedule = App.dotenv["SCHEDULE"].toString()
        val time = App.dotenv["TIME"].toString()
        val jobId = App.dotenv["JOBID"].toString()
        val jobGroup = groupName

        val cronObj = JSONObject()
        cronObj.put("group", jobGroup)
        cronObj.put("job_id", jobId)
        cronObj.put("schedule", schedule)
        cronObj.put("time", time)
        breadcrumb.log("CRON SCHEDULE: $cronObj")

        val cronSchedule = convertCronSchedule(
            breadcrumb,
            schedule,
            time
        )

        scheduler.deleteJob(JobKey.jobKey("job$jobId", "group$jobGroup"))
        val triggerKey = TriggerKey("cronTrigger$jobId", "group$jobGroup")
        scheduler.unscheduleJob(triggerKey)

        val job = JobBuilder.newJob(CronSmsService::class.java)
            .withIdentity("job$jobId", "group$jobGroup")
            .usingJobData("data", cronObj.toString())
            .build()

        val trigger: Trigger = TriggerBuilder.newTrigger()
            .withIdentity("cronTrigger$jobId", "group$jobGroup")
            .withSchedule(
                CronScheduleBuilder.cronSchedule(cronSchedule)
                    .inTimeZone(TimeZone.getTimeZone("Asia/Manila"))
            )
            .forJob(job)
            .build()
        scheduler.scheduleJob(job, trigger)

        val allParcelSchedule = "EVERYDAY"
        val allParcelTime = "00:00"
        val allParcelJobId = "CRON00002"
        val allParcelJobGroup = "all_parcel"

        val allParcelCronObj = JSONObject()
        allParcelCronObj.put("group", allParcelJobGroup)
        allParcelCronObj.put("job_id", allParcelJobId)
        allParcelCronObj.put("schedule", allParcelSchedule)
        allParcelCronObj.put("time", allParcelTime)
        breadcrumb.log("CRON SCHEDULE: $allParcelCronObj")

        val allParcelCronSchedule = convertCronSchedule(
            breadcrumb,
            allParcelSchedule,
            allParcelTime

        )

        scheduler.deleteJob(JobKey.jobKey("job$allParcelJobId", "group$allParcelJobGroup"))
        val allParcelTriggerKey = TriggerKey("cronTrigger$allParcelJobId", "group$allParcelJobGroup")
        scheduler.unscheduleJob(allParcelTriggerKey)

        val allParcelJob = JobBuilder.newJob(AllParcelCron::class.java)
            .withIdentity("job$allParcelJobId", "group$allParcelJobGroup")
            .usingJobData("data", allParcelCronObj.toString())
            .build()

        val allParcelTrigger: Trigger = TriggerBuilder.newTrigger()
            .withIdentity("cronTrigger$allParcelJobId", "group$allParcelJobGroup")
            .withSchedule(
                CronScheduleBuilder.cronSchedule(allParcelCronSchedule)
                    .inTimeZone(TimeZone.getTimeZone("Asia/Manila"))
            )
            .forJob(allParcelJob)
            .build()
        scheduler.scheduleJob(allParcelJob, allParcelTrigger)

        for (group in scheduler.jobGroupNames) {
            for (jobKey in scheduler.getJobKeys(GroupMatcher.groupEquals(group))) {
                breadcrumb.log("CHECK JOB AFTER RUNNING: $jobKey")
            }
        }
    }

    private fun convertCronSchedule(breadcrumb: Breadcrumb, schedule: String, time: String): String {
        breadcrumb.log("START: CONVERT SCHEDULE ($schedule - $time) TO CRON")
        var cronSchedule = "0 MIN HR DATE * DAY"
        val time = time.split(":")
        cronSchedule = cronSchedule.replace("HR", time[0])
        cronSchedule = cronSchedule.replace("MIN", time[1])

        cronSchedule = when (schedule) {

            "EVERYDAY" -> {
                cronSchedule.replace("DAY", "?").replace("DATE", "*")
            }

            "MID" -> {
                cronSchedule.replace("DAY", "?").replace("DATE", "15")
            }

            "END" -> {
                cronSchedule.replace("DAY", "?").replace("DATE", "28,29,30,31")
            }

            else -> {
                cronSchedule.replace("DAY", schedule).replace("DATE", "?")
            }
        }
        breadcrumb.log("END: CONVERT SCHEDULE TO CRON - $cronSchedule")
        return cronSchedule
    }

    private fun fetchParcel() {
            val breadcrumb = Breadcrumb()
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
                App.allParcel = fetch.fetchBulkData(breadcrumb, "{concierge_name: { ${MongoOperator.ne}: '' },  created_at:{${MongoOperator.gte}: '${App.dateStart}', ${MongoOperator.lte}: '${App.dateEnd}'}}")
                val query = "{created_at:{${MongoOperator.gte}: '${App.dateStart}', ${MongoOperator.lte}: '${App.dateEnd}'}}"
                App.allPickUp = pickUp.fetchBulkData(breadcrumb, query)
            }
    }
}