package app.services.aps.permit

import app.utils.Breadcrumb
import app.utils.MongoDbLogs
import app.utils.mongoDB.Fetch
import io.javalin.http.Context
import org.json.JSONObject

class GetRequestPermit {
    private val dbLogs = MongoDbLogs()
    private val fetch = Fetch()
    fun fetchRequestPermit(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS GET REQUESTED PERMIT")
        val response = JSONObject()
        val composeResponse = JSONObject()
        val unitNo = req.queryParam("unit_no").toString()
        breadcrumb.log("UNIT NO $unitNo")

        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, JSONObject(), "user.id", "GET REQUESTED PERMITS")
        try {
            val permits = fetch.fetchRequestPermit(breadcrumb, unitNo)
            if (permits.isNotEmpty()) {
                composeResponse.put("success", true)
                composeResponse.put("data", permits)

            } else {
                composeResponse.put("success", false)
                composeResponse.put("message", "No Request Permit Found!")
            }
            response.put("HttpStatus", 200)

        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Error on fetching permit!")

            response.put("HttpStatus", 502)
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", token, response, "user.id", "GET REQUESTED PERMITS")
        response.put("response", composeResponse)
        return response
    }

    fun fetchAllRequestPermit(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS GET ALL REQUESTED PERMIT")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, JSONObject(), "user.id", "GET ALL REQUESTED PERMITS")
        try {
            val permits = fetch.fetchAllRequestPermit(breadcrumb)
            if (permits.isNotEmpty()) {
                composeResponse.put("success", true)
                composeResponse.put("data", permits)

            } else {
                composeResponse.put("success", false)
                composeResponse.put("message", "No Request Permit Found!")
            }
            response.put("HttpStatus", 200)

        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Error on fetching permit!")

            response.put("HttpStatus", 502)
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", token, response, "user.id", "GET ALL REQUESTED PERMITS")
        response.put("response", composeResponse)
        return response
    }

    fun filterPermit(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS GET ALL REQUESTED PERMIT")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val body = JSONObject(req.body())
        breadcrumb.log("REQUEST BODY $body")

        val permitId = body.optString("permit_id")
        breadcrumb.log("PERMIT ID $permitId")

        val startDate = body.optString("start")
        breadcrumb.log("START DATE $startDate")

        val endDate = body.optString("end")
        breadcrumb.log("END DATE $endDate")

        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, JSONObject(), "user.id", "GET PERMIT ID REQUESTED PERMITS")
        try {
            val permits = fetch.filterPermit(breadcrumb, body)
            if (permits.isNotEmpty()) {
                composeResponse.put("success", true)
                composeResponse.put("data", permits)

            } else {
                composeResponse.put("success", false)
                composeResponse.put("message", "No Request Permit Found!")
            }
            response.put("HttpStatus", 200)

        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Error on fetching permit!")

            response.put("HttpStatus", 502)
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", token, response, "user.id", "GET PERMIT ID REQUESTED PERMITS")
        response.put("response", composeResponse)
        return response
    }

    fun viewPermitDetails(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS GET REQUESTED PERMIT")
        val response = JSONObject()
        val composeResponse = JSONObject()
        val permitId = req.queryParam("permit_id").toString()
        breadcrumb.log("UNIT NO $permitId")

        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, JSONObject(), "user.id", "GET REQUESTED PERMITS")
        try {
            val permits = fetch.viewPermitRequestDetails(breadcrumb, permitId)
            if (!permits.isEmpty) {
                composeResponse.put("success", true)
                composeResponse.put("data", permits)

            } else {
                composeResponse.put("success", false)
                composeResponse.put("message", "No Request Permit Found!")
            }
            response.put("HttpStatus", 200)

        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Error on fetching permit!")

            response.put("HttpStatus", 502)
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", token, response, "user.id", "GET REQUESTED PERMITS")
        response.put("response", composeResponse)
        return response
    }
}