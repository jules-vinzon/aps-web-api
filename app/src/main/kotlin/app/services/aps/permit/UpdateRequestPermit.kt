package app.services.aps.permit

import app.utils.Breadcrumb
import app.utils.MongoDbLogs
import app.utils.mongoDB.Update
import io.javalin.http.Context
import org.bson.Document
import org.json.JSONObject

class UpdateRequestPermit {
    private val dbLogs = MongoDbLogs()
    private val update = Update()
    fun updateRequestPermit(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS UPDATE REQUEST PERMIT")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val body = JSONObject(req.body())
        breadcrumb.log("REQUEST BODY $body")

        val permitId = req.queryParam("permit_id").toString()
        breadcrumb.log("PERMIT ID $permitId")

        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, body, "user.id", "UPDATE REQUEST PERMIT")
        try {
            breadcrumb.log("UPDATE PERMIT ID $permitId")
            val filter = Document("permit_id", permitId)
            val updateRes = update.updateData(breadcrumb, body, filter, "permit_request", permitId)
            if (!updateRes.optBoolean("success")) {
                response.put("HttpStatus", 400)
            } else {
                response.put("HttpStatus", 200)
            }
            response.put("response", updateRes)
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Server error!")

            response.put("HttpStatus", 500)
            response.put("response", composeResponse)
            e.printStackTrace()
        }
        breadcrumb.log("DONE UPDATE PERMIT ID $permitId")
        breadcrumb.log("RESPONSE $composeResponse")
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", token, response, "user.id", "UPDATE REQUEST PERMIT")
        return response
    }

    fun approveRequestPermit(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS UPDATE REQUEST PERMIT")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val body = JSONObject(req.body())
        breadcrumb.log("REQUEST BODY $body")

        val permitId = body.optString("permit_id")
        breadcrumb.log("PERMIT ID $permitId")

        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, body, "user.id", "UPDATE REQUEST PERMIT")
        breadcrumb.log("TOKEN $token")
        try {
            breadcrumb.log("UPDATE PERMIT ID $permitId")
            val updateRes = update.approveDocument(breadcrumb, body, permitId)
            if (!updateRes.optBoolean("success")) {
                response.put("HttpStatus", 400)
            } else {
                response.put("HttpStatus", 200)
            }
            response.put("response", updateRes)
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Server error!")

            response.put("HttpStatus", 500)
            response.put("response", composeResponse)
            e.printStackTrace()
        }
        breadcrumb.log("DONE UPDATE PERMIT ID $permitId")
        breadcrumb.log("RESPONSE $composeResponse")
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", token, response, "user.id", "UPDATE REQUEST PERMIT")
        return response
    }
}