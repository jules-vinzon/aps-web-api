package app.services.aps.permit

import app.App
import app.utils.Breadcrumb
import app.utils.Helpers
import app.utils.MongoDbLogs
import app.utils.Mqtt
import app.utils.mongoDB.Fetch
import app.utils.mongoDB.Saving
import io.javalin.http.Context
import org.bson.Document
import org.bson.types.ObjectId
import org.json.JSONArray
import org.json.JSONObject

class RequestPermit {
    private val dbLogs = MongoDbLogs()
    private val helpers = Helpers()
    private val mongoSave = Saving()
    private val fetch = Fetch()
    private val mqtt = Mqtt()
    fun requestPermit(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS REQUEST PERMIT")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val body = JSONObject(req.body())
        breadcrumb.log("REQUEST BODY $body")
        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, body, "user.id", "REQUEST PERMIT")
        breadcrumb.log("TOKEN $token")
        try {
            val reqFields = arrayListOf("user_id", "permit_id", "permit_type", "data", "unit_no")
            val validate = helpers.validateRequest(breadcrumb, body, reqFields, "request permit")
            if (!validate.optBoolean("valid")) {
                return validate
            }

            val reqPermit = saveRequestPermit(breadcrumb, body, token)
            if (!reqPermit.optBoolean("success")) {
                response.put("HttpStatus", 400)
            } else {
                response.put("HttpStatus", 200)
            }
            response.put("response", reqPermit)
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Server error!")

            response.put("HttpStatus", 500)
            e.printStackTrace()
        }
        return response
    }

    private fun saveRequestPermit(breadcrumb: Breadcrumb, body: JSONObject, token: String): JSONObject {
        breadcrumb.log("SAVING ON PERMIT TABLE")
        val response = JSONObject()
        val collection = "permit_request"
        try {
            val approvers = fetch.fetchPermitApprover(breadcrumb, body.optString("permit_type"))
            val data = Document()
            data["user_id"] = ObjectId(body.optString("user_id"))
            data["permit_id"] = body.optString("permit_id")
            data["permit_type"] = ObjectId(body.optString("permit_type"))
            data["unit_no"] = body.optString("unit_no")
            data["data"] = Document.parse(body.optString("data"))
            data["approver"] = approvers
            data["status"] = "FOR VERIFICATION"

            dbLogs.saveAuditlogs("SAVING", "DATA TO BE SAVE", token, JSONObject(data), "user.id", "ADD PERMIT")
            val isSaved = mongoSave.save(breadcrumb, data, collection)
            dbLogs.saveAuditlogs("SAVING", "DONE SAVING", token, JSONObject(data), "user.id", "ADD PERMIT")
            if (isSaved) {
                val id = approvers.indexOfFirst { it["rank"] == 1 }
                publishMqtt(breadcrumb, approvers[id]["position_id"].toString(), body)

                breadcrumb.log("SAVED ON PERMIT TABLE")
                response.put("message", "Saved Successfully!")
                response.put("permit_id", body.optString("permit_id"))
                response.put("success", true)
            } else {
                breadcrumb.log("UNABLE TO SAVE ON PERMIT TABLE")
                response.put("message", "Save Unsuccessful!")
                response.put("success", false)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            val error = JSONObject().put("error", e.message)
            dbLogs.saveAuditlogs("SAVING", "FAILED TO SAVE", token, error, "user.id", "ADD PERMIT")

            response.put("message", "Server Error!")
            response.put("success", false)

            breadcrumb.log("FAILED TO SAVE ON PERMIT TABLE")
        }
        return response
    }

    private fun publishMqtt(breadcrumb: Breadcrumb, approver: String, body: JSONObject) {
        breadcrumb.log("START PUBLISHING MQTT REQUEST")
        val secret = App.dotenv["SECRET_KEY"].toString()
        val topic = "$secret/$approver"
        val message = JSONObject()
        val permitId = body.optString("permit_id")
        val permitType = body.optString("permit_type")
        val unitNo = body.optString("unit_no")
        val pdata = fetch.fetchPermits(breadcrumb)
        val id = pdata.indexOfFirst { it["id"] == permitType }

        message.put("permit_id", permitId)
        message.put("permit_type", pdata[id]["permit"].toString())
        message.put("current_status", "FOR VERIFICATION")
        message.put("unit_no", unitNo)
        message.put("notif_id", "TEST0002")
        message.put("read", false)

        mqtt.publish(breadcrumb, topic, message.toString())
        breadcrumb.log("END PUBLISHING MQTT REQUEST")
    }
}