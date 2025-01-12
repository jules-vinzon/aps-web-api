package app.services.aps.role

import app.utils.Breadcrumb
import app.utils.MongoDbLogs
import app.utils.mongoDB.Fetch
import io.javalin.http.Context
import org.json.JSONObject

class FetchUserRole {
    private val dbLogs = MongoDbLogs()
    private val fetch = Fetch()
    fun fetchTenantRole(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START FETCHING USER ROLE")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, JSONObject(), "user.id", "FETCH USER ROLE")
        try {
            val role = fetch.getUserRole(breadcrumb, token)
            if (!role.isEmpty) {
                composeResponse.put("success", true)
                composeResponse.put("token", token)
                composeResponse.put("data", role)

                response.put("HttpStatus", 200)
            } else {
                composeResponse.put("success", false)
                composeResponse.put("message", "No Role Found!")
            }
            response.put("httpStatus", 200)
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Server error!")

            response.put("HttpStatus", 500)
            e.printStackTrace()
        }
        response.put("response", composeResponse)
        return response
    }
}