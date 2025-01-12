package app.services.aps.permit

import app.utils.Breadcrumb
import app.utils.MongoDbLogs
import app.utils.mongoDB.Update
import io.javalin.http.Context
import org.bson.Document
import org.bson.types.ObjectId
import org.json.JSONObject

class UpdatePermit {
    private val dbLogs = MongoDbLogs()
    private val update = Update()
    fun updatePermit(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS UPDATE REQUEST PERMIT")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val id = req.queryParam("id").toString()
        breadcrumb.log("PERMIT ID $id")

        val body = JSONObject(req.body())
        breadcrumb.log("REQUEST BODY $body")
        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, body, "user.id", "UPDATE PERMIT")
        breadcrumb.log("TOKEN $token")
        try {
            breadcrumb.log("UPDATE ID $id")
            val filter = Document("_id", ObjectId(id))
            val updateRes = update.updateData(breadcrumb, body, filter, "permit", id)
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
        breadcrumb.log("DONE UPDATE ID $id")
        breadcrumb.log("RESPONSE $composeResponse")
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", token, response, "user.id", "UPDATE PERMIT")
        return response
    }
}