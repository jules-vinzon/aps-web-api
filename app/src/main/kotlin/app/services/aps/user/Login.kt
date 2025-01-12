package app.services.aps.user

import app.utils.Breadcrumb
import app.utils.Crypt
import app.utils.Helpers
import app.utils.mongoDB.Fetch
import app.utils.MongoDbLogs
import app.utils.mongoDB.Saving
import io.javalin.http.Context
import org.bson.Document
import org.bson.types.ObjectId
import org.json.JSONObject

class Login {
    private val dbLogs = MongoDbLogs()
    private val crypt = Crypt()
    private val fetch = Fetch()
    private val helpers = Helpers()
    private val mongoSaving = Saving()
    fun login(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS TENANT LOGIN")
        val response = JSONObject()
        val body = JSONObject(req.body())
        breadcrumb.log("body $body")

        val reqId = body.optString("request_id")
        val composeResponse = JSONObject()
        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", reqId, body, "user.id", "TENANT LOGIN")
        breadcrumb.log("REQUEST ID $reqId")
        try {
            val validate = validateRequest(breadcrumb, body)
            breadcrumb.log("VALIDATE RESPONSE $validate")
            if (!validate.optBoolean("valid")) {
                return validate
            }

            val decBody = validate.optJSONObject("reqBody")
            val username = decBody.optString("username")
            var password = decBody.optString("password")
            password = crypt.hashPassword(password)
            val check = fetch.login(breadcrumb, username, password)
            if (check.isEmpty) {
                composeResponse.put("success", false)
                composeResponse.put("message", "Invalid username or password!")

                response.put("HttpStatus", 401)
                response.put("response", composeResponse)
                return response
            } else {
                val id = check.optString("id")
                val token = crypt.generateToken(reqId, username)
                tokenSaving(breadcrumb, token, reqId, id)

                composeResponse.put("success", true)
                composeResponse.put("token", token)
                composeResponse.put("data", check)
                composeResponse.put("message", "Login Success!")
                response.put("HttpStatus", 200)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            composeResponse.put("success", false)
            composeResponse.put("message", "Server error!")

            response.put("HttpStatus", 500)
        }
        breadcrumb.log("END PROCESS TENANT LOGIN")
        response.put("response", composeResponse)
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", reqId, response, "user.id", "TENANT LOGIN")
        return response
    }

    private fun validateRequest(breadcrumb: Breadcrumb, body: JSONObject): JSONObject {
        val composeResponse = JSONObject()
        val response = JSONObject()
        val requestId = body.optString("request_id")
        breadcrumb.log("START VALIDATING REQUEST ID")
        if (requestId.isEmpty()) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Missing request_id!")

            breadcrumb.log("INVALID REQUEST ID")

            response.put("HttpStatus", 400)
            response.put("response", composeResponse)
            response.put("valid", false)
            return response
        }

        breadcrumb.log("CHECK IF PRIVATE KEY IS EXISTING")
        val check = fetch.fetchJson(breadcrumb, "keys", "{request_id: '${requestId}'}")
        if (check.isEmpty) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Invalid token")

            breadcrumb.log("INVALID REQUEST ID")

            response.put("HttpStatus", 401)
            response.put("response", composeResponse)
            response.put("valid", false)
            return response
        }
        breadcrumb.log("DONE VALIDATING REQUEST ID")

        breadcrumb.log("START DECODING REQUEST BODY")
        val privKey = check.optString("private_key")

        val encdata = body.optString("encdata")
        val dec = crypt.decrypt(breadcrumb, privKey, encdata)
        val req = JSONObject(dec.optString("decrypted", JSONObject().toString()))
        if (!dec.optBoolean("valid")) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Unable to decrypt request body!")

            response.put("HttpStatus", 401)
            response.put("response", composeResponse)
            response.put("valid", false)
            return response
        }
        breadcrumb.log("DONE DECODING REQUEST BODY")

        if (req.optString("username").isEmpty()) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Missing username!")

            response.put("HttpStatus", 400)
            response.put("response", composeResponse)
            response.put("valid", false)
            return response
        }

        if (req.optString("password").isEmpty()) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Missing password!")

            response.put("HttpStatus", 400)
            response.put("response", composeResponse)
            response.put("valid", false)
            return response
        }
        req.put("request_id", requestId)
        response.put("reqBody", req)
        response.put("valid", true)
        return response
    }

    private fun tokenSaving(breadcrumb: Breadcrumb, token: String, requestId: String, id: String) {
        breadcrumb.log("START SAVING TOKEN!")
        try {
            val collection = "active_token"
            val data = Document()
            data["token"] = token
            data["request_id"] = requestId
            data["user_id"] = ObjectId(id)
            mongoSaving.save(breadcrumb, data, collection)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        breadcrumb.log("END SAVING TOKEN!")
    }

    fun validateToken(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS VALIDATE TOKEN")
        val response = JSONObject()
        val body = JSONObject(req.body())
        breadcrumb.log("body $body")

        val reqId = body.optString("request_id")
        val token = body.optString("token")
        val composeResponse = JSONObject()
        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", reqId, body, "user.id", "TENANT LOGIN")
        breadcrumb.log("REQUEST ID $reqId")

        try {
            val validateToken = crypt.validateJwtToken(token)
            if (validateToken) {
                composeResponse.put("success", true)
                composeResponse.put("message", "Valid Token!")
            } else {
                composeResponse.put("success", false)
                composeResponse.put("message", "Invalid Token!")
            }
            breadcrumb.log("VALIDATE TOKEN, $validateToken")
        } catch (e: Throwable) {
            e.printStackTrace()
            composeResponse.put("success", false)
            composeResponse.put("message", "Server error!")

            response.put("HttpStatus", 500)
        }
        breadcrumb.log("END PROCESS TENANT LOGIN")
        response.put("response", composeResponse)
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", reqId, response, "user.id", "TENANT LOGIN")
        return response
    }
}