package app.services.aps.permit

import app.utils.Breadcrumb
import app.utils.Helpers
import app.utils.MongoDbLogs
import app.utils.mongoDB.Saving
import io.javalin.http.Context
import org.bson.Document
import org.bson.types.ObjectId
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class AddPermit {
    private val dbLogs = MongoDbLogs()
    private val helpers = Helpers()
    private val mongoSave = Saving()
    fun addPermit(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS ADD PERMIT")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val body = JSONObject(req.body())
        breadcrumb.log("REQUEST BODY $body")
        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, body, "user.id", "ADD PERMIT")
        breadcrumb.log("TOKEN $token")
        try {
            val reqFields = arrayListOf("permit", "approver")
            val validate = helpers.validateRequest(breadcrumb, body, reqFields, "add permit")
            if (!validate.optBoolean("valid")) {
                return validate
            }

            val addPermit = savePermit(breadcrumb, body, token)
            if (!addPermit.optBoolean("success")) {
                response.put("HttpStatus", 400)
            } else {
                response.put("HttpStatus", 200)
            }
            response.put("response", addPermit)
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Server error!")

            response.put("HttpStatus", 500)
            e.printStackTrace()
        }
        return response
    }

    private fun savePermit(breadcrumb: Breadcrumb, body: JSONObject, token: String): JSONObject {
        breadcrumb.log("SAVING ON PERMIT TABLE")
        val response = JSONObject()
        val collection = "permit"
        try {
            val approver = approverParser(body.optJSONArray("approver"))
            val data = Document()
            data["permit"] = body.optString("permit").uppercase(Locale.getDefault())
            data["approver"] = approver
            data["status"] = "ACTIVE"

            dbLogs.saveAuditlogs("SAVING", "DATA TO BE SAVE", token, JSONObject(data), "user.id", "ADD PERMIT")
            val isSaved = mongoSave.save(breadcrumb, data, collection)
            dbLogs.saveAuditlogs("SAVING", "DONE SAVING", token, JSONObject(data), "user.id", "ADD PERMIT")
            if (isSaved) {
                breadcrumb.log("SAVED ON PERMIT TABLE")
                response.put("message", "Saved Successfully!")
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

    private fun approverParser(permit: JSONArray): ArrayList<Document> {
        val parse = ArrayList<Document>()
        permit.forEach {
            val json = JSONObject(it.toString())
            val jsonParse = Document()
            json.keySet().sorted().forEach { param ->
                when (param) {
                    "position_id" -> jsonParse[param] = ObjectId(json.optString(param))
                    "rank" -> jsonParse[param] = json.optInt(param)
                    else -> jsonParse[param] = json.optString(param)
                }
            }
            parse.add(jsonParse)
        }
        return parse
    }
}