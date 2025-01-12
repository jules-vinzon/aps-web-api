package app.services.aps.permit

import app.utils.Breadcrumb
import app.utils.MongoDbLogs
import app.utils.mongoDB.Fetch
import io.javalin.http.Context
import org.json.JSONObject

class GetPermits {
    private val dbLogs = MongoDbLogs()
    private val fetch = Fetch()
    fun getPermits(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS GET PERMIT TYPES")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, JSONObject(), "user.id", "GET PERMITS")
        try {
            val permits = fetch.fetchPermits(breadcrumb)
            if (permits.isNotEmpty()) {
                composeResponse.put("success", true)
                composeResponse.put("data", permits)
            } else {
                composeResponse.put("success", false)
                composeResponse.put("message", "No Permit Found!")
            }
            response.put("HttpStatus", 200)
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Error on fetching permit!")

            response.put("HttpStatus", 502)
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", token, response, "user.id", "GET PERMITS")
        response.put("response", composeResponse)
        return response
    }
}