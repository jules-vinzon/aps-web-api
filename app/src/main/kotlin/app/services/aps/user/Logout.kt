package app.services.aps.user

import app.utils.Breadcrumb
import app.utils.MongoDbLogs
import app.utils.mongoDB.Delete
import io.javalin.http.Context
import org.json.JSONObject

class Logout {
    private val dbLogs = MongoDbLogs()
    private val delete = Delete()
    fun logout(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS TENANT LOGIN")
        val response = JSONObject()
        val body = JSONObject(req.body())
        breadcrumb.log("body $body")

        val token = body.optString("token")
        val composeResponse = JSONObject()
        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, body, "user.id", "TENANT LOGIN")
        breadcrumb.log("REQUEST ID $token")
        try {
            val delete = delete.deleteOne(breadcrumb, "active_token", "{token: '${token}'}")
            if (delete) {
                composeResponse.put("status", true)
                composeResponse.put("message", "Logged out!")
            } else {
                response.put("HttpStatus", 400)
                composeResponse.put("status", false)
                composeResponse.put("message", "Token does not exist!")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            composeResponse.put("success", false)
            composeResponse.put("message", "Server error!")

            response.put("HttpStatus", 500)
        }
        response.put("response", composeResponse)
        return response
    }
}