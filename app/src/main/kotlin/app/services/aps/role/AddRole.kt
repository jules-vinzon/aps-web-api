package app.services.aps.role

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

class AddRole {
    private val dbLogs = MongoDbLogs()
    private val helpers = Helpers()
    private val mongoSave = Saving()
    fun addRole(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS ADD MODULE")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val body = JSONObject(req.body())
        breadcrumb.log("REQUEST BODY $body")
        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, body, "user.id", "ADD MODULE")
        breadcrumb.log("TOKEN $token")
        try {
            val reqFields = arrayListOf("role", "modules")
            val validate = helpers.validateRequest(breadcrumb, body, reqFields, "add role")
            if (!validate.optBoolean("valid")) {
                return validate
            }

            val addRole = saveRole(breadcrumb, body, token)
            if (!addRole.optBoolean("success")) {
                response.put("HttpStatus", 400)
                response.put("response", addRole)
            } else {
                response.put("HttpStatus", 200)
                response.put("response", addRole)
            }
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Server error!")

            response.put("HttpStatus", 500)
            e.printStackTrace()
        }
        return response
    }

    private fun saveRole(breadcrumb: Breadcrumb, body: JSONObject, token: String): JSONObject {
        breadcrumb.log("SAVING ON ROLE TABLE")
        val response = JSONObject()
        val collection = "role"
        try {
            val data = Document()
            val modules = moduleParser(body.optJSONArray("modules"))
            data["role"] = body.optString("role").uppercase(Locale.getDefault())
            data["modules"] = modules
            data["status"] = "ACTIVE"

            dbLogs.saveAuditlogs("SAVING", "DATA TO BE SAVE", token, JSONObject(data), "user.id", "ADD ROLE")
            val isSaved = mongoSave.save(breadcrumb, data, collection)
            dbLogs.saveAuditlogs("SAVING", "DONE SAVING", token, JSONObject(data), "user.id", "ADD ROLE")
            if (isSaved) {
                breadcrumb.log("SAVED ON ROLE TABLE")
                response.put("message", "Saved Successfully!")
                response.put("success", true)
            } else {
                breadcrumb.log("UNABLE TO SAVE ON ROLE TABLE")
                response.put("message", "Save Unsuccessful!")
                response.put("success", false)
            }
        } catch(e : Throwable) {
            e.printStackTrace()
            val error = JSONObject().put("error", e.message)
            dbLogs.saveAuditlogs("SAVING", "FAILED TO SAVE", token, error, "user.id", "ADD ROLE")

            response.put("message", "Server Error!")
            response.put("success", false)

            breadcrumb.log("FAILED TO SAVE ON ROLE TABLE")
        }
        return response
    }

    private fun moduleParser(module: JSONArray): ArrayList<Document> {
        return module.map {
            val json = JSONObject(it.toString())
            val parse = Document()
            json.keySet().sorted().forEach { param ->
                when (param) {
                    "module_id" ->
                        parse[param] = ObjectId(json.optString(param))
                    "submodules" ->
                        parse[param] = moduleParser(json.optJSONArray(param))
                    else ->
                        parse[param] = Document.parse(json.optString(param))
                }
            }
            parse
        }.toCollection(ArrayList())
    }
}