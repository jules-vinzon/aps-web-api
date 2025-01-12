package app.services.aps.employee

import app.utils.Breadcrumb
import app.utils.Crypt
import app.utils.Helpers
import app.utils.MongoDbLogs
import app.utils.mongoDB.Saving
import io.javalin.http.Context
import org.bson.Document
import org.bson.types.ObjectId
import org.json.JSONObject
import java.util.*

class Register {
    private val dbLogs = MongoDbLogs()
    private val helpers = Helpers()
    private val mongoSave = Saving()
    private val crypt = Crypt()
    fun register(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS REGISTER EMPLOYEE")
        val response = JSONObject()
        val composeResponse = JSONObject()

        val body = JSONObject(req.body())
        breadcrumb.log("REQUEST BODY $body")
        val token = req.header("token").toString()
        breadcrumb.log("TOKEN $token")

        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", token, body, "user.id", "REGISTER EMPLOYEE")
        try {
            val reqFields = arrayListOf("username", "password", "first_name", "last_name", "employee_no", "mobile_no", "email")
            val validate = helpers.validateRequest(breadcrumb, body, reqFields, "register_employee")
            if (!validate.optBoolean("valid")) {
                return validate
            }

            val authId = saveUserTable(breadcrumb, body, token)
            if (!authId.optBoolean("success")) {
                response.put("HttpStatus", 400)
                response.put("response", authId)
            }

            val isSaveTenantDetails = saveEmployee(breadcrumb, body, authId.optString("id"))
            if (!isSaveTenantDetails.optBoolean("success")) {
                response.put("HttpStatus", 400)
                response.put("response", isSaveTenantDetails)
            } else {
                response.put("HttpStatus", 200)
                response.put("response", isSaveTenantDetails)
            }
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Server error!")

            response.put("HttpStatus", 500)
            e.printStackTrace()
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", token, response, "user.id", "REGISTER EMPLOYEE")
        return response
    }

    private fun saveUserTable(breadcrumb: Breadcrumb, body: JSONObject, token: String): JSONObject {
        breadcrumb.log("SAVING ON PARCEL TABLE")
        val response = JSONObject()
        val collection = "auth"
        try {
            val password = crypt.hashPassword(body.optString("password"))
            val data = Document()
            data["username"] = body.optString("username")
            data["password"] = password
            data["account_status"] = "ACTIVE"

            dbLogs.saveAuditlogs("SAVING", "DATA TO BE SAVE", token, JSONObject(data), "user.id", "ADD AUTH")
            val authId = mongoSave.saveObjectId(breadcrumb, data, collection)
            dbLogs.saveAuditlogs("SAVING", "DONE SAVING", token, JSONObject(data), "user.id", "ADD AUTH")
            if (authId.isEmpty()) {
                breadcrumb.log("UNABLE TO SAVE ON AUTH TABLE")
                response.put("message", "Save Unsuccessful!")
                response.put("success", false)
            } else {
                response.put("id", authId)
            }
        } catch(e : Throwable) {
            e.printStackTrace()
            val error = JSONObject().put("error", e.message)
            dbLogs.saveAuditlogs("SAVING", "FAILED TO SAVE", token, error, "user.id", "ADD AUTH")

            response.put("message", "Server Error!")
            response.put("success", false)
            response.put("error", e.message)

            breadcrumb.log("FAILED TO SAVE ON AUTH TABLE")
        }
        return response
    }

    private fun saveEmployee(breadcrumb: Breadcrumb, body: JSONObject, userId: String): JSONObject {
        breadcrumb.log("SAVING ON EMPLOYEE TABLE")
        val response = JSONObject()
        val collection = "employee"
        try {
            val data = Document()
            val fname = body.optString("first_name").uppercase(Locale.getDefault())
            val mname = body.optString("middle_name").uppercase(Locale.getDefault())
            val lname = body.optString("last_name").uppercase(Locale.getDefault())
            data["first_name"] = fname
            data["middle_name"] = mname
            data["last_name"] = lname
            data["full_name"] = "$fname ${if (mname.isNotEmpty()) "$mname " else ""}$lname"
            data["nickname"] = body.optString("nickname").uppercase(Locale.getDefault())
            data["employee_no"] = body.optString("employee_no")
            data["mobile"] = body.optString("mobile")
            data["email"] = body.optString("email")
            data["auth_id"] = ObjectId(userId)
            data["position_id"] = ObjectId(body.optString("position_id"))
            data["role_id"] = ObjectId(userId)
            data["status"] = "ACTIVE"

            dbLogs.saveAuditlogs("SAVING", "DATA TO BE SAVE", userId, JSONObject(data), "user.id", "ADD EMPLOYEE")
            val isSaved = mongoSave.save(breadcrumb, data, collection)
            dbLogs.saveAuditlogs("SAVING", "DONE SAVING", userId, JSONObject(data), "user.id", "ADD EMPLOYEE")
            if (!isSaved) {
                breadcrumb.log("UNABLE TO SAVE ON EMPLOYEE TABLE")
                response.put("message", "Save Unsuccessful!")
                response.put("success", false)
            } else {
                breadcrumb.log("SAVED ON RESIDENT TABLE")
                response.put("message", "Saved Successfully!")
                response.put("success", true)
            }
        } catch(e : Throwable) {
            e.printStackTrace()
            val error = JSONObject().put("error", e.message)
            dbLogs.saveAuditlogs("SAVING", "FAILED TO SAVE", userId, error, "user.id", "ADD EMPLOYEE")

            response.put("message", "Server Error!")
            response.put("success", false)
            response.put("error", e.message)

            breadcrumb.log("FAILED TO SAVE ON EMPLOYEE TABLE")
        }
        return response
    }
}