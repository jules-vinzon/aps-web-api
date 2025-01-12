package app.services.aps.unit

import app.utils.Breadcrumb
import app.utils.MongoDbLogs
import app.utils.mongoDB.Fetch
import org.json.JSONObject
import io.javalin.http.Context

class FetchUnit {
    private val dbLogs = MongoDbLogs()
    private val fetch = Fetch()
    fun getUnit(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS GET MODULE")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, JSONObject(), "user.id", "GET UNIT")
        try {
            val unitList = fetch.fetchArray(breadcrumb, "units", "{status: 'ACTIVE'}")
            if (unitList.isNotEmpty()) {
                val units = arrayListOf<String>()
                unitList.forEach {
                    units.add(it.optString("unit_no"))
                }
                units.sort()
                composeResponse.put("success", true)
                composeResponse.put("data", units)
            } else {
                composeResponse.put("success", false)
                composeResponse.put("message", "No Role Found!")
            }
            response.put("HttpStatus", 200)
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Error on fetching role!")

            response.put("HttpStatus", 502)
            e.printStackTrace()
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", token, response, "user.id", "GET UNIT")
        response.put("response", composeResponse)
        return response
    }
}