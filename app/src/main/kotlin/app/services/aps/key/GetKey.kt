package app.services.aps.key
import app.App
import app.utils.Breadcrumb
import app.utils.Crypt
import app.utils.mongoDB.Fetch
import app.utils.MongoDbLogs
import org.json.JSONObject
import io.javalin.http.Context
import org.bson.Document
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class GetKey {
    val fetch = Fetch()
    val dbLogs = MongoDbLogs()
    val crypt = Crypt()
    fun getKey(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("START PROCESS GET KEY")
        val body = JSONObject(req.body())
        val requestId = body.optString("request_id")
        breadcrumb.log("REQUEST ID $requestId")
        val response = JSONObject()
        val composeResponse = JSONObject()
        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", requestId, body, "user.id", "GENERATE KEY")
        try {
            if (requestId.isEmpty()) {
                composeResponse.put("status", true)
                composeResponse.put("message", "Unable to decrypt request body!")

                dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", requestId, response, "user.id", "GENERATE KEY")

                response.put("response", composeResponse)
                response.put("HttpStatus", 400)
            }

            val check = fetch.fetchJson(breadcrumb, "keys", "{request_id: '${requestId}'}")
            breadcrumb.log("CHECK IF EXISTING $check")
            if (check.isEmpty) {
                breadcrumb.log("NO RECORD FOUND")
                val keys = crypt.generateKeypair()
                breadcrumb.log("KEY $keys")

                saveKeys(breadcrumb, keys.optString("public"), keys.optString("private"), requestId)
                composeResponse.put("success", true)
                composeResponse.put("request_id", requestId)
                composeResponse.put("public_key", keys.optString("public"))
            } else {
                composeResponse.put("success", true)
                composeResponse.put("request_id", requestId)
                composeResponse.put("public_key", check.optString("public_key"))
            }
            response.put("HttpStatus", 200)
        } catch (e: Throwable) {
            e.printStackTrace()
            composeResponse.put("success", false)
            composeResponse.put("request_id", requestId)
            composeResponse.put("message", "Server error!")

            response.put("HttpStatus", 502)
        }
        response.put("response", composeResponse)
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", requestId, response, "user.id", "GENERATE KEY")
        return response
    }

    private fun saveKeys(breadcrumb: Breadcrumb, pubKey: String, privKey: String, reqId: String) {
        try {
            breadcrumb.log("START SAVING KEYS")
            val dateTimeNow = DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString()

            val payload = Document()

            payload["request_id"] = reqId
            payload["public_key"] = pubKey
            payload["private_key"] = privKey
            payload["created_at"] = dateTimeNow
            payload["updated_at"] = dateTimeNow

            val col = App.mongoDatabase.getCollection("keys")
            col.insertOne(payload)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        breadcrumb.log("END SAVING KEYS")
    }
}