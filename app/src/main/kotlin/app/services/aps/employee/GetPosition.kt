package app.services.aps.employee

import app.utils.Breadcrumb
import app.utils.mongoDB.Fetch
import app.utils.MongoDbLogs
import org.json.JSONObject

class GetPosition {
    private val dbLogs = MongoDbLogs()
    private val fetch = Fetch()
    fun getPosition(breadcrumb: Breadcrumb, reqId: String): JSONObject {
        breadcrumb.log("START PROCESS GET POSITION")
        val response = JSONObject()
        val composeResponse = JSONObject()
        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", reqId, JSONObject(), "user.id", "GET POSITION")
        try {
            val positionList = fetch.fetchArray(breadcrumb, "position", "{status: 'ACTIVE'}")
            if (positionList.isNotEmpty()) {
                composeResponse.put("success", true)
                composeResponse.put("data", positionList)

            } else {
                composeResponse.put("success", false)
                composeResponse.put("message", "No Position Found!")

            }
            response.put("HttpStatus", 200)
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Error on fetching position!")

            response.put("HttpStatus", 502)
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", reqId, response, "user.id", "GET POSITION")
        response.put("response", composeResponse)
        return response
    }
}