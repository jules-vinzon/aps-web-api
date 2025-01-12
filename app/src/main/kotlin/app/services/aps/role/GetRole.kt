package app.services.aps.role

import app.utils.Breadcrumb
import app.utils.MongoDbLogs
import app.utils.mongoDB.Fetch
import org.json.JSONObject

class GetRole {
    private val dbLogs = MongoDbLogs()
    private val fetch = Fetch()
    fun getRole(breadcrumb: Breadcrumb, reqId: String): JSONObject {
        breadcrumb.log("START PROCESS GET MODULE")
        val response = JSONObject()
        val composeResponse = JSONObject()
        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", reqId, JSONObject(), "user.id", "GET ROLE")
        try {
            val roleList = fetch.getAllRole(breadcrumb)
            if (roleList.isNotEmpty()) {
                composeResponse.put("success", true)
                composeResponse.put("data", roleList)
            } else {
                composeResponse.put("success", false)
                composeResponse.put("message", "No Role Found!")
            }
            response.put("HttpStatus", 200)
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Error on fetching role!")

            response.put("HttpStatus", 502)
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", reqId, response, "user.id", "GET ROLE")
        response.put("response", composeResponse)
        return response
    }
}