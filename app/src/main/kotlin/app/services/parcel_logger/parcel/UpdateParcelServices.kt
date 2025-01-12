package app.services.parcel_logger.parcel

import app.App
import org.bson.Document
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject
import app.utils.Breadcrumb
import app.utils.SmsSender
import io.javalin.http.Context
import java.util.*
import kotlin.collections.ArrayList

class UpdateParcelServices {

    fun updateParcel(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("Entering UPDATE PARCEL Function")
        val response = JSONObject()
        val result = JSONObject()
        val body = JSONObject(req.body())
        val data = JSONObject()
        val parcelId = body.optString("parcel_id")
        try {
            val parcelData = FetchParcelServices().getParcel(breadcrumb, body.optString("parcel_id"))
            if (!parcelData.isEmpty) {
                val isUpdated = updateParcelRecord(breadcrumb, body)
                if (isUpdated) {
                    val smsStatus = body.optString("parcel_status", "RECEIVED")
                    SmsSender().sendSms(parcelData, smsStatus.lowercase(Locale.getDefault()))
                    data.put("parcel_id", parcelId)
                    data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
                    data.put("message", "Parcel $parcelId Successfully Updated!")
                    response.put("status", "SUCCESS")
                } else {
                    data.put("parcel_id", body.optString("parcel_id"))
                    data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
                    data.put("message", "Failed To Update Parcel ${body.optString("parcel_id")}!")
                    response.put("status", "FAILED")
                }
            } else {
                data.put("parcel_id", body.optString("parcel_id"))
                data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
                data.put("message", "NO PARCEL $parcelId FOUND!")
                response.put("status", "FAILED")
            }
            result.put("HttpStatus", 200)
        } catch (e: Throwable) {
            data.put("parcel_id", body.optString("parcel_id"))
            data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
            data.put("message", "Failed to update parcel ${body.optString("parcel_id")}")
            result.put("HttpStatus", 400)
            response.put("status", "FAILED")
        }
        breadcrumb.log("Response : $response")
        response.put("data", data)
        result.put("response", response)
        return result
    }

    fun updateBulkParcel(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("Entering UPDATE PARCEL Function")
        val result = JSONObject()
        val response = JSONObject()
        val body = JSONObject(req.body())
        val data = JSONObject()
        breadcrumb.log("REQUEST BODY $body");
        try {
            val parcel = body.optJSONArray("parcel")
            val parcelUpdate: ArrayList<String> = ArrayList()
            parcel.forEach{ parcelId ->
                body.put("parcel_id", parcelId.toString())
                val isUpdated = updateParcelRecord(breadcrumb, body)
                if (isUpdated) {
                    parcelUpdate.add(parcelId.toString())
                }
            }

            if (body.optString("status").uppercase(Locale.getDefault()) == "RECEIVED" ) {
                if (parcelUpdate.isNotEmpty()) {
                    val parcelData = FetchParcelServices().getParcel(breadcrumb, parcelUpdate.first())
                    body.put("parcel_id", parcelUpdate)
                    body.put("recipients_mobile_no", parcelData.optString("recipients_mobile_no"))
                    body.put("receiver_name", parcelData.optString("receiver_name"))
                    SmsSender().sendSms(body, "received")
                }
            }

            if (parcelUpdate.isNotEmpty()) {
                data.put("parcel_id", parcelUpdate)
                data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
                data.put("message", "Parcel $parcelUpdate Successfully Updated!")
                response.put("status", "SUCCESS")
            } else {
                data.put("parcel_id", parcelUpdate)
                data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
                data.put("message", "Parcel $parcelUpdate failed to update!")
                response.put("status", "FAILED")
            }

            result.put("HttpStatus", 200)
        } catch (e: Throwable) {
            e.printStackTrace()
            data.put("parcel_id", body.optString("parcel_id"))
            data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
            data.put("message", "Failed to update parcel ${body.optString("parcel_id")}")
            result.put("HttpStatus", 400)
            response.put("status", "FAILED")
        }
        breadcrumb.log("Response : $response")
        response.put("data", data)
        result.put("response", response)
        return result
    }

    fun updateParcelRecord(breadcrumb: Breadcrumb, body: JSONObject): Boolean {
        return try {
            breadcrumb.log("UPDATING ON PARCEL TABLE")
            val manilaTime = DateTimeZone.forID("Asia/Manila")
            val dt = DateTime(manilaTime).toString()
            val col = App.mongoDatabase.getCollection("parcel")
            val reqStatus = body.optString("parcel_status", "RECEIVED")
            val status = Document("parcel_status", reqStatus)
                .append("updated_at", dt)
            if(body.optString("parcel_owner_datetime_received") != "") {
                status.append("parcel_owner_datetime_received", body.optString("parcel_owner_datetime_received"))
            }
            if (body.optString("parcel_owner_received_signature") != "") {
                status.append("parcel_owner_received_signature", body.optString("parcel_owner_received_signature"))
            }
            if (body.optString("concierge_employee_id") != "") {
                status.append("concierge_employee_id", body.optString("concierge_employee_id"))
            }
            if (body.optString("concierge_datetime_received") != "") {
                status.append("concierge_datetime_received", body.optString("concierge_datetime_received"))
            }
            if (body.optString("concierge_name") != "") {
                status.append("concierge_name", body.optString("concierge_name"))
            }
            if (body.optString("received_by_employee_id") != "") {
                status.append("received_by_employee_id", body.optString("received_by_employee_id"))
            }
            if (body.optString("received_by_employee_name") != "") {
                status.append("received_by_employee_name", body.optString("received_by_employee_name"))
            }
            if (body.optString("receiver_name") != "") {
                status.append("receiver_name", body.optString("receiver_name"))
            }
            if (body.optString("released_parcel_photo") != "") {
                status.append("released_parcel_photo", body.optString("released_parcel_photo"))
            }
            if (body.optString("reason") != "") {
                status.append("reason", body.optString("reason"))
            }
            if (body.optString("datetime_declined") != "") {
                status.append("datetime_declined", body.optString("datetime_declined"))
            }
            val filter = Document("parcel_id", body.optString("parcel_id"))
            val updateFieldDoc = Document("\$set",status)
            col.updateOne(filter, updateFieldDoc).modifiedCount
            breadcrumb.log("UPDATED ON PARCEL TABLE")
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            breadcrumb.log("FAILED TO UPDATE ON PARCEL TABLE")
            false
        }
    }
}