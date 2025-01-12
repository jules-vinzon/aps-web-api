package app.services.aps.module

import app.utils.Breadcrumb
import app.utils.Helpers
import app.utils.MongoDbLogs
import app.utils.mongoDB.Saving
import io.javalin.http.Context
import org.bson.Document
import org.bson.types.ObjectId
import org.json.JSONObject
import java.util.*

class AddModule {
    private val dbLogs = MongoDbLogs()
    private val helpers = Helpers()
    private val mongoSave = Saving()
    fun addModule(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS ADD MODULE")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val body = JSONObject(req.body())
        breadcrumb.log("REQUEST BODY $body")
        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, body, "user.id", "ADD MODULE")
        breadcrumb.log("REQUEST ID $token")
        try {

            val reqFields = arrayListOf("icon", "position", "name")
            val validate = helpers.validateRequest(breadcrumb, body, reqFields, "add module")
            if (!validate.optBoolean("valid")) {
                return validate
            }

            val addModule = saveModule(breadcrumb, body, token)
            if (!addModule.optBoolean("success")) {
                response.put("HttpStatus", 400)
                response.put("response", addModule)
            } else {
                response.put("HttpStatus", 200)
                response.put("response", addModule)
            }
        } catch (e:Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Server error!")

            response.put("HttpStatus", 500)
            e.printStackTrace()
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", token, response, "user.id", "ADD MODULE")
        breadcrumb.log("START PROCESS ADD MODULE")
        return response
    }

    private fun saveModule(breadcrumb: Breadcrumb, body: JSONObject, token: String): JSONObject {
        breadcrumb.log("SAVING ON MODULE TABLE")
        val response = JSONObject()
        val collection = "module"
        try {
            val data = Document()
            data["icon"] = body.optString("icon")
            data["position"] = body.optInt("position")
            data["name"] = body.optString("name").uppercase(Locale.getDefault())
            data["status"] = "ACTIVE"

            dbLogs.saveAuditlogs("SAVING", "DATA TO BE SAVE", token, JSONObject(data), "user.id", "ADD MODULE")
            val isSaved = mongoSave.save(breadcrumb, data, collection)
            dbLogs.saveAuditlogs("SAVING", "DONE SAVING", token, JSONObject(data), "user.id", "ADD MODULE")
            if (isSaved) {
                breadcrumb.log("SAVED ON MODULE TABLE")
                response.put("message", "Saved Successfully!")
                response.put("success", true)
            } else {
                breadcrumb.log("UNABLE TO SAVE ON MODULE TABLE")
                response.put("message", "Save Unsuccessful!")
                response.put("success", false)
            }
        } catch(e : Throwable) {
            e.printStackTrace()
            val error = JSONObject().put("error", e.message)
            dbLogs.saveAuditlogs("SAVING", "FAILED TO SAVE", token, error, "user.id", "ADD MODULE")

            response.put("message", "Server Error!")
            response.put("success", false)

            breadcrumb.log("FAILED TO SAVE ON MODULE TABLE")
        }
        return response
    }
}