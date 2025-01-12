package app.services.aps.employee

import app.utils.Breadcrumb
import app.utils.Helpers
import app.utils.MongoDbLogs
import app.utils.mongoDB.Fetch
import app.utils.mongoDB.Saving
import io.javalin.http.Context
import org.bson.Document
import org.json.JSONObject
import org.litote.kmongo.MongoOperator
import java.util.*

class AddPosition {
    private val dbLogs = MongoDbLogs()
    private val fetch = Fetch()
    private val mongoSave = Saving()
    private val helpers = Helpers()

    fun addPosition(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS ADD POSITION")
        val response = JSONObject()
        val body = JSONObject(req.body())
        val reqId = body.optString("request_id")
        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", reqId, body, "user.id", "ADD POSITION")
        breadcrumb.log("REQUEST ID $reqId")
        try {
            val reqFields = arrayListOf("position_code", "position_name")
            val validate = helpers.validateRequest(breadcrumb, body, reqFields, "add position")
            if (!validate.optBoolean("valid")) {
                return validate
            }

            val savingResponse = savePosition(breadcrumb, body)
            if (savingResponse.optBoolean("success")) {
                response.put("HttpStatus", 200)
            } else {
                response.put("HttpStatus", 400)
            }
            response.put("response", savingResponse)
        } catch (e:Throwable) {
            e.printStackTrace()
            val err = JSONObject()
            err.put("message", "Server Error!")
            err.put("success", false)
            response.put("response", err)
            response.put("HttpStatus", 500)
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", reqId, response, "user.id", "ADD POSITION")
        breadcrumb.log("START PROCESS ADD POSITION")
        return response
    }

    private fun savePosition(breadcrumb: Breadcrumb, body: JSONObject): JSONObject {
        breadcrumb.log("SAVING ON POSITION TABLE")
        val response = JSONObject()
        val reqId = body.optString("request_id")
        val collection = "position"
        try {
            val code = body.optString("position_code").lowercase(Locale.getDefault())
            val name = body.optString("position_name").uppercase(Locale.getDefault())
            val data = Document()
            data["position_code"] = code
            data["position_name"] = name
            data["status"] = "ACTIVE"

            dbLogs.saveAuditlogs("SAVING", "DATA TO BE SAVE", reqId, JSONObject(data), "user.id", "ADD POSITION")
            val isSaved = mongoSave.save(breadcrumb, data, collection)
            dbLogs.saveAuditlogs("SAVING", "DONE SAVING", reqId, JSONObject(data), "user.id", "ADD POSITION")
            if (isSaved) {
                breadcrumb.log("SAVED ON POSITION TABLE")
                response.put("message", "Saved Successfully!")
                response.put("success", true)
            } else {
                breadcrumb.log("UNABLE TO SAVE ON POSITION TABLE")
                response.put("message", "Save Unsuccessful!")
                response.put("success", false)
            }
        } catch(e : Throwable) {
            e.printStackTrace()
            val error = JSONObject().put("error", e.message)
            dbLogs.saveAuditlogs("SAVING", "FAILED TO SAVE", reqId, error, "user.id", "ADD POSITION")

            response.put("message", "Position Name or Position Code already exist!")
            response.put("success", false)
            response.put("error", e.message)

            breadcrumb.log("FAILED TO SAVE ON POSITION TABLE")
        }
        return response
    }
}