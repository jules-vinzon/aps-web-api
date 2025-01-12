package app.services.aps.unit

import app.utils.Breadcrumb
import app.utils.Helpers
import app.utils.MongoDbLogs
import app.utils.mongoDB.Saving
import io.javalin.http.Context
import org.bson.Document
import org.json.JSONObject

class AddUnit {
    private val dbLogs = MongoDbLogs()
    private val helpers = Helpers()
    private val mongoSave = Saving()
    fun addUnit(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS ADD UNIT")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val body = JSONObject(req.body())
        breadcrumb.log("REQUEST BODY $body")
        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, body, "user.id", "ADD UNIT")
        try {
            val reqFields = arrayListOf("unit_no")
            val validate = helpers.validateRequest(breadcrumb, body, reqFields, "add unit")
            if (!validate.optBoolean("valid")) {
                return validate
            }

            val saveUnit = saveUnitNo(breadcrumb, body, token)
            if (!saveUnit.optBoolean("success")) {
                response.put("HttpStatus", 400)
                response.put("response", saveUnit)
            } else {
                response.put("HttpStatus", 200)
                response.put("response", saveUnit)
            }
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Server error!")

            response.put("HttpStatus", 500)
            e.printStackTrace()
        }
        return response
    }

    private fun saveUnitNo(breadcrumb: Breadcrumb, body: JSONObject, token: String): JSONObject {
        breadcrumb.log("SAVING ON UNIT TABLE")
        val response = JSONObject()
        val collection = "units"
        try {
            val data = Document()
            data["unit_no"] = body.optString("unit_no")
            data["status"] = "ACTIVE"

            dbLogs.saveAuditlogs("SAVING", "DATA TO BE SAVE", token, JSONObject(data), "user.id", "ADD UNIT")
            val isSaved = mongoSave.save(breadcrumb, data, collection)
            dbLogs.saveAuditlogs("SAVING", "DONE SAVING", token, JSONObject(data), "user.id", "ADD UNIT")
            if (isSaved) {
                breadcrumb.log("SAVED ON UNIT TABLE")
                response.put("message", "Saved Successfully!")
                response.put("success", true)
            } else {
                breadcrumb.log("UNABLE TO SAVE ON UNIT TABLE")
                response.put("message", "Save Unsuccessful!")
                response.put("success", false)
            }
        } catch(e : Throwable) {
            e.printStackTrace()
            val error = JSONObject().put("error", e.message)
            dbLogs.saveAuditlogs("SAVING", "FAILED TO SAVE", token, error, "user.id", "ADD UNIT")

            response.put("message", "Server Error!")
            response.put("success", false)

            breadcrumb.log("FAILED TO SAVE ON UNIT TABLE")
        }
        return response
    }
}