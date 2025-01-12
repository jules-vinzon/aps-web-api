package app.services.parcel_logger.pickUp

import app.App
import app.utils.Breadcrumb
import app.utils.MongoDbLogs
import io.javalin.http.Context
import org.bson.Document
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject

class UpdatePickUp {
    private val dbLogs = MongoDbLogs()
    fun pickUpUpdate(breadcrumb: Breadcrumb, req: Context): JSONObject {
        val breadcrumbId = breadcrumb.log("Update Pick Up Parcel")
        breadcrumb.log("req body: ${req.body()}")
        val response = JSONObject()
        val result = JSONObject()
        val body = JSONObject(req.body())
        val data = JSONObject()
        val pickUpId = body.optString("pickup_id")
        dbLogs.saveToRequestResponseLogs("REQUEST", "REQUEST BODY", breadcrumbId, body, "pickup_logs")

        try {
            val pickUpData = FetchPickUp().fetchParcel(breadcrumb, body.optString("pickup_id"))
            if(!pickUpData.isEmpty) {
                val isUpdated = updatePickUpRecord(breadcrumb, body)
                if (isUpdated) {
                    dbLogs.saveToRequestResponseLogs("UPDATE", "UPDATE SUCCESS", breadcrumbId, body, "pickup_logs")
                    data.put("pickup_id", pickUpId)
                    data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
                    data.put("message", "pickup $pickUpId Successfully Updated!")
                    response.put("status", "SUCCESS")
                } else {
                    dbLogs.saveToRequestResponseLogs("UPDATE", "UPDATE FAILED", breadcrumbId, body, "pickup_logs")
                    data.put("pickup_id", body.optString("pickup_id"))
                    data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
                    data.put("message", "Failed To Update pickup ${body.optString("pickup_id")}!")
                    response.put("status", "FAILED")
                }
            } else {
                dbLogs.saveToRequestResponseLogs("UPDATE", "PICKUP ID NOT EXIST!", breadcrumbId, body, "pickup_logs")
                data.put("pickup_id", body.optString("pickup_id"))
                data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
                data.put("message", "NO PARCEL $pickUpId FOUND!")
                response.put("status", "FAILED")
            }
            result.put("HttpStatus", 200)
        } catch (e: Throwable) {
            data.put("pickup_id", body.optString("pickup_id"))
            data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
            data.put("message", "Failed to update pickup ${body.optString("pickup_id")}")
            result.put("HttpStatus", 400)
            response.put("status", "FAILED")

        }

        breadcrumb.log("Response : $response")
        response.put("data", data)
        result.put("response", response)
        dbLogs.saveToRequestResponseLogs("RESPONSE", "RESPONSE BODY", breadcrumbId, response, "pickup_logs")
        return result
    }

    private fun updatePickUpRecord(breadcrumb: Breadcrumb, body: JSONObject): Boolean {
        return try {
            breadcrumb.log("UPDATING ON PICKUP_PARCEL TABLE")
            val manilaTime = DateTimeZone.forID("Asia/Manila")
            val dt = DateTime(manilaTime).toString()
            val col = App.mongoDatabase.getCollection("pickup_parcel")
            val reqStatus = body.optString("status", "RECEIVED")
            val status = Document("status", reqStatus)
                .append("updated_at", dt)
            if (body.optString("received_by_employee_name") != "") {
                status.append("received_by_employee_name", body.optString("received_by_employee_name"))
            }
            if (body.optString("employee_no") != "") {
                status.append("employee_no", body.optString("employee_no"))
            }
            if (body.optString("employee_name") != "") {
                status.append("employee_name", body.optString("employee_name"))
            }
            if (body.optString("received_by_employee_id") != "") {
                status.append("received_by_employee_id", body.optString("received_by_employee_id"))
            }
            if (body.optString("datetime_completed") != "") {
                status.append("datetime_completed", body.optString("datetime_completed"))
            }
            if (body.optString("datetimestamp") != "") {
                status.append("datetimestamp", body.optString("datetimestamp"))
            }
            if (body.optString("signature") != "") {
                status.append("signature", body.optString("signature"))
            }
            if (body.optString("receiver_name") != "") {
                status.append("receiver_name", body.optString("receiver_name"))
            }
            if (body.optString("plate_number") != "") {
                status.append("plate_number", body.optString("plate_number"))
            }
            val filter = Document("pickup_id", body.optString("pickup_id"))
            val updateFieldDoc = Document("\$set",status)
            col.updateOne(filter, updateFieldDoc).modifiedCount
            breadcrumb.log("UPDATED ON PICKUP_PARCEL TABLE")
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            breadcrumb.log("FAILED TO UPDATE ON PICKUP_PARCEL TABLE")
            false
        }
    }
}
