package app.services.parcel_logger.pickUp

import app.App
import app.App.Companion.pickupNo
import app.utils.Breadcrumb
import app.utils.MongoDbLogs
import io.javalin.http.Context
import org.bson.Document
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject
import org.litote.kmongo.findOne
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

class AddPickUp {
    val dbLogs = MongoDbLogs()
    fun pickUpParcel(breadcrumb: Breadcrumb, req: Context): JSONObject {
        val breadcrumbId = breadcrumb.log("Pick Up parcel")
        val response = JSONObject()
        val result = JSONObject()
        val body = JSONObject(req.body())
        val data = JSONObject()
        val savedParcel = ArrayList<String>()
        val failedParcel = ArrayList<String>()
        val existingParcel = ArrayList<String>()
        dbLogs.saveToRequestResponseLogs("REQUEST", "REQUEST BODY", breadcrumbId, body, "pickup_logs")
        breadcrumb.log("req body $body $breadcrumbId")
        breadcrumb.log("req body $breadcrumbId")
        try {
            val pickupData = body.optJSONArray("pickup_details")
            pickupData.forEach {context ->
                val data = JSONObject(context.toString())
                val funResp = saveParcel(breadcrumb, data)
                if (funResp.optBoolean("status")) {
                    savedParcel.add(funResp.optString("pickup_id"))
                } else {
                    failedParcel.add(funResp.optString("pickup_id"))
                }
            }
            data.put("successfully_saved", savedParcel)
            data.put("successfully_updated", existingParcel)
            data.put("failed", failedParcel)
            data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
            data.put("message", "Success")
            result.put("HttpStatus", 200)
            dbLogs.saveToRequestResponseLogs("RESPONSE", "RESPONSE BODY", breadcrumbId, data, "pickup_logs")
        } catch(e: Throwable){
            e.printStackTrace()
            data.put("pickup_id", body.optString("pickup_id"))
            data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
            data.put("message", "Failed to save parcel ${body.optString("pickup_id")}")
            result.put("HttpStatus", 400)
            response.put("status", "FAILED")
        }
        breadcrumb.log("Response : $response")
        response.put("data", data)
        result.put("response", response)
        return result
    }

    private fun getPickup(breadcrumb: Breadcrumb, pickupId: String): JSONObject {
        breadcrumb.log("FETCH ON PARCEL TABLE")
        return try {
            val parcelData = App.mongoDatabase.getCollection("pickup_parcel")
            val parcelDataObject = parcelData.findOne("{pickup_id:'${pickupId}'}")
            breadcrumb.log("FETCHED DATA: $parcelDataObject")
            if (parcelDataObject != null) {
                JSONObject(parcelDataObject)
            } else {
                JSONObject()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            breadcrumb.log("NO DATA FETCHED")
            JSONObject()
        }
    }

    private fun saveParcel(breadcrumb: Breadcrumb, body: JSONObject): JSONObject {
        val response = JSONObject()
        val pickupIdVal = if (body.optString("pickup_id") == "") {
            val unitNo = body.optString("unit_number")
            val parcelId = generatePickupId(unitNo, body.optString("pickup_id"))
            body.put("pickup_id", parcelId)
            parcelId
        } else {
            body.optString("pickup_id")
        }
        try {
            val breadcrumbId = breadcrumb.log("SAVING ON PICKUP_PARCEL TABLE")
            dbLogs.saveToRequestResponseLogs("SAVING", "DATA TO BE SAVED", breadcrumbId, body, "pickup_logs")
            val col = App.mongoDatabase.getCollection("pickup_parcel")
            val manilaTime = DateTimeZone.forID("Asia/Manila")
            val dt = DateTime(manilaTime).toString()
            pickupNo = generatePickupNo()
            val dtime =  if (body.optString("datetimestamp") == "") {
                dt
            } else {
                body.optString("datetimestamp")
            }
            val isExisting = getPickup(breadcrumb, pickupIdVal)
            if (isExisting.isEmpty) {
                val data = Document()
                data["pickup_id"] = pickupIdVal
                data["pickup_no"] = pickupNo
                data["name_of_sender"] = body.optString("name_of_sender")
                data["receiver_type"] = body.optString("receiver_type")
                data["courier_type"] = body.optString("courier_type")
                data["unit_number"] = body.optString("unit_number")
                data["description_of_items"] = body.optString("description_of_items")
                data["employee_no"] = body.optString("employee_no")
                data["employee_name"] = body.optString("employee_name")
                data["datetimestamp"] = dtime
                data["datetime_completed"] = body.optString("datetime_completed")
                data["status"] = body.optString("status" , "PENDING").uppercase(Locale.getDefault())
                data["received_by_employee_name"] = body.optString("received_by_employee_name")
                data["received_by_employee_id"] = body.optString("received_by_employee_id")
                data["signature"] = body.optString("signature")
                data["receiver_name"] = body.optString("receiver_name")
                data["quantity"] = body.optString("quantity", "1")
                data["sender_type"] = body.optString("sender_type")
                data["plate_number"] = body.optString("plate_number")
                data["is_synced"] = true
                data["created_at"] = dt
                data["updated_at"] = dt
                col.insertOne(data)
                breadcrumb.log("SAVED ON PICKUP_PARCEL TABLE")
                response.put("status", true)
            } else {
                response.put("status", false)
            }
        } catch(e : Throwable) {
            e.printStackTrace()
            breadcrumb.log("FAILED TO SAVE ON PICKUP_PARCEL TABLE")
            response.put("status", false)
        }
        response.put("pickup_id", pickupIdVal)
        return response
    }

    private fun generatePickupId(unitNumber: String, pickup: String): String {
        var pickupId = pickup.ifEmpty {
            "P-" + unitNumber + Instant.now().epochSecond
        }
        val collection = App.mongoDatabase.getCollection("pickup_parcel")
        val parcelDataObject = collection.findOne("{pickup_id:'${pickupId}'}")
        if (parcelDataObject != null) {
            pickupId= generatePickupId(unitNumber, pickup)
        }
        return pickupId
    }

    private fun generatePickupNo(): String {
        val regex = "(\\D*)(\\d+)".toRegex()
        val matchResult = regex.matchEntire(pickupNo)
        return  if (matchResult != null) {
            val prefix = matchResult.groups[1]?.value ?: ""
            val number = matchResult.groups[2]?.value?.toIntOrNull() ?: 0
            val formattedNumber = String.format("%06d", number+1)
            "$prefix${formattedNumber}"
        } else {
            pickupNo
        }
    }
}
