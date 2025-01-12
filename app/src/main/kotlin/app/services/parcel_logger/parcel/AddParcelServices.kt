package app.services.parcel_logger.parcel

import app.App
import app.App.Companion.parcelNo
import app.utils.Breadcrumb
import app.utils.SmsSender
import io.javalin.http.Context
import org.bson.Document
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class AddParcelServices {
    val update = UpdateParcelServices()
    fun addParcel(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("Entering ADD PARCEL Function")
        val response = JSONObject()
        val result = JSONObject()
        val body = JSONObject(req.body())
        val data = JSONObject()
        try {
            val parcelData = FetchParcelServices().getParcel(breadcrumb, body.optString("parcel_id"))
            val dh = DateTime().toDateTime((DateTimeZone.forID("Asia/Manila"))).toString("MMddYYYYHH")
            var riderName = body.optString("riders_name")
            riderName = riderName.replace(" ", "")
            val riderDetails = "R-${riderName.uppercase(Locale.getDefault())}${dh}"
            breadcrumb.log("RIDER QR DETAILS: $dh")
            if (parcelData.isEmpty) {
                val isSaved = saveParcel(breadcrumb, body, riderDetails)
                if (isSaved) {
                    data.put("parcel_id", body.optString("parcel_id"))
                    data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
                    data.put("message", "Parcel ${body.optString("parcel_id")} Successfully Saved!")
                    response.put("status", "SUCCESS")
                } else {
                    data.put("parcel_id", body.optString("parcel_id"))
                    data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
                    data.put("message", "Failed to save parcel ${body.optString("parcel_id")}")
                    response.put("status", "FAILED")
                }
            } else {
                data.put("parcel_id", body.optString("parcel_id"))
                data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
                data.put("message", "PARCEL ID ALREADY EXIST!")
                response.put("status", "FAILED")
            }
            result.put("HttpStatus", 200)
        } catch (e: Throwable) {
            e.printStackTrace()
            data.put("parcel_id", body.optString("parcel_id"))
            data.put("timestamp", DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString())
            data.put("message", "Failed to save parcel ${body.optString("parcel_id")}")
            result.put("HttpStatus", 400)
            response.put("status", "FAILED")
        }

        breadcrumb.log("Response : $response")
        response.put("data", data)
        result.put("response", response)
        return result
    }

    fun addBulkParcel(breadcrumb: Breadcrumb, req: Context): JSONObject {
        breadcrumb.log("Entering ADD PARCEL Function")
        val body = JSONObject(req.body())
        val result = JSONObject()
        val data = ArrayList<JSONObject>()
        val response = JSONObject()
        val mnlTime = DateTimeZone.forID("Asia/Manila")
        breadcrumb.log("RECIPIENT DETAILS: $body")
        try {
            val dateTime = DateTime(mnlTime)
            val dh = dateTime.millis / 1000
            val recipientData = body.optJSONArray("rider_details")

            val ownerName = distinctData(recipientData, "recipients_name")
            // ARRANGE DATA PER OWNER
            ownerName.forEach {context ->
                // FETCH TO ARRAY WITH THE SAME OWNER NAME
                val parcelData = filterJSON(recipientData, context.toString())
                val riderDetails = parcelData.optJSONArray("rider_details")
                val parcelIDReceived = ArrayList<String>()
                val parcelIDNew = ArrayList<String>()
                riderDetails.forEach {
                    val parcels = JSONObject(it.toString())
                    val parcelId = parcels.optString("parcel_id")

                    // SAVING PARCEL
                    val savingResponse = savingJSON(breadcrumb, parcels, dh)
                    data.add(savingResponse)

                    if (parcels.optString("parcel_status") == "RECEIVED") {
                        parcelIDReceived.add(parcelId)
                    }
                    if(parcels.optString("parcel_status") == "PENDING") {
                        if (parcels.optString("concierge_name") != "") {
                            parcelIDNew.add(parcelId)
                        }
                    }
                }
                breadcrumb.log("PARCEL RECEIVED $parcelIDReceived")
                breadcrumb.log("PARCEL NEW $parcelIDNew")
                if (parcelIDReceived.isNotEmpty()) {
                    val parcelFromDB = FetchParcelServices().getParcel(breadcrumb, parcelIDReceived.first())
                    val sms = JSONObject()
                    sms.put("parcel_id", parcelIDReceived)
                    sms.put("recipients_mobile_no", parcelFromDB.optString("recipients_mobile_no"))
                    sms.put("receiver_name", parcelFromDB.optString("receiver_name"))
                    SmsSender().sendSms(sms, "received")
                }
                if (parcelIDNew.isNotEmpty()) {
                    val parcelFromDB = riderDetails.first().toString()
                    val sms = JSONObject()
                    sms.put("parcel_id", parcelIDNew)
                    sms.put("recipients_mobile_no", JSONObject(parcelFromDB).optString("recipients_mobile_no"))
                    sms.put("receiver_name", JSONObject(parcelFromDB).optString("receiver_name"))
                    SmsSender().sendSms(sms, "new")
                }
            }
            response.put("status", "SUCCESS")
            result.put("HttpStatus", 200)
        } catch (e: Throwable) {
            e.printStackTrace()
            val toResponse = JSONObject()
            toResponse.put("parcel_id", body.optString("parcel_id"))
            toResponse.put("timestamp", DateTime(mnlTime).toString())
            toResponse.put("message", "Failed to save parcel ${body.optString("parcel_id")}")
            data.add(toResponse)
            result.put("HttpStatus", 400)
            response.put("status", "FAILED")
        }
        breadcrumb.log("Response : $response")
        response.put("data", data)
        result.put("response", response)
        return result
    }

    private fun savingJSON(breadcrumb: Breadcrumb, parcelData: JSONObject, dh: Long):JSONObject {
        val toResponse = JSONObject()
        val mnlTime = DateTimeZone.forID("Asia/Manila")

        // USED TO GENERATE RIDER DETAILS
        val parcelId = parcelData.optString("parcel_id")
        var riderName = parcelData.optString("riders_name")
        riderName = riderName.replace(" ", "")

        // GENERATE RIDER DETAILS
        val riderDetails = "R-${riderName.uppercase(Locale.getDefault())}${dh}"
        breadcrumb.log("RIDER QR DETAILS: $dh")

        // CHECK IF NEW PARCEL
        val parcelFromDB = FetchParcelServices().getParcel(breadcrumb, parcelId)
        if (parcelFromDB.isEmpty) {
            val isSaved = saveParcel(breadcrumb, parcelData, riderDetails)
            if (isSaved) {
                toResponse.put("parcel_id", parcelId)
                toResponse.put("rider_qr_details", riderDetails)
                toResponse.put("timestamp", DateTime(mnlTime).toString())
                toResponse.put("message", "Parcel $parcelId Successfully Saved!")
                toResponse.put("status", "SUCCESS")
            } else {
                toResponse.put("parcel_id", parcelData.optString("parcel_id"))
                toResponse.put("rider_qr_details", riderDetails)
                toResponse.put("timestamp", DateTime(mnlTime).toString())
                toResponse.put("message", "Failed to save parcel ${parcelData.optString("parcel_id")}")
                toResponse.put("status", "FAILED")
            }
        } else {
            val isUpdated = update.updateParcelRecord(breadcrumb, parcelData)
            if (isUpdated) {
                toResponse.put("parcel_id", parcelId)
                toResponse.put("rider_qr_details", riderDetails)
                toResponse.put("timestamp", DateTime(mnlTime).toString())
                toResponse.put("message", "Parcel $parcelId Successfully Updated!")
                toResponse.put("status", "SUCCESS")
            } else {
                toResponse.put("parcel_id", parcelData.optString("parcel_id"))
                toResponse.put("rider_qr_details", riderDetails)
                toResponse.put("timestamp", DateTime(mnlTime).toString())
                toResponse.put("message", "Failed to update parcel ${parcelData.optString("parcel_id")}")
                toResponse.put("status", "FAILED")
            }
        }
        return toResponse
    }

    private fun saveParcel(breadcrumb: Breadcrumb, body: JSONObject, qrDetails: String): Boolean {
        try {
            breadcrumb.log("SAVING ON PARCEL TABLE")
            val col = App.mongoDatabase.getCollection("parcel")
            val manilaTime = DateTimeZone.forID("Asia/Manila")
            val dt = DateTime(manilaTime).toString()
            val datetime = body.optString("datetimestamp")
            val cDtR = body.optString("concierge_datetime_received")
            val pDtr = body.optString("parcel_owner_datetime_received")
            parcelNo = generateParcelNo()
            val data = Document()
            data["parcel_id"] = body.optString("parcel_id")
            data["riders_name"] = body.optString("riders_name")
            data["rider_qr_details"] = body.optString("rider_qr_details", qrDetails)
            data["riders_courier_type"] = body.optString("riders_courier_type")
            data["recipients_description_of_items"] = body.optString("recipients_description_of_items")
            data["recipients_unit_number"] = body.optString("recipients_unit_number")
            data["recipients_name"] = body.optString("recipients_name")
            data["recipients_mobile_no"] = body.optString("recipients_mobile_no")
            data["parcel_owner_received_signature"] = body.optString("parcel_owner_received_signature", null)
            data["concierge_employee_id"] = body.optString("concierge_employee_id")
            data["concierge_name"] = body.optString("concierge_name")
            data["parcel_status"] = body.optString("parcel_status")
            data["parcel_no"] = body.optString("parcel_no", parcelNo)
            data["datetimestamp"] = parseDate(datetime)
            data["concierge_datetime_received"] = parseDate(cDtR)
            data["received_by_employee_id"] = body.optString("received_by_employee_id")
            data["received_by_employee_name"] = body.optString("received_by_employee_name")
            data["receiver_name"] = body.optString("receiver_name")
            data["parcel_owner_datetime_received"] = body.optString("parcel_owner_datetime_received")
            data["released_parcel_photo"] = body.optString("released_parcel_photo")
            data["reason"] = body.optString("reason")
            data["datetime_declined"] = body.optString("datetime_declined")
            data["is_synced"] = true
            data["created_at"] = dt
            data["updated_at"] = dt
            col.insertOne(data)
            breadcrumb.log("SAVED ON PARCEL TABLE")

            return true
        } catch(e : Throwable) {
            e.printStackTrace()
            breadcrumb.log("FAILED TO SAVE ON PARCEL TABLE")
            return false
        }
    }

    private fun distinctData (recipientData: JSONArray, parameter: String): List<Any> {
        val parameterValues = mutableListOf<Any>()

        for (i in 0 until recipientData.length()) {
            val jsonObject = recipientData.getJSONObject(i)
            if (jsonObject.has(parameter)) {
                val parameterValue = jsonObject.get(parameter)
                parameterValues.add(parameterValue)
            }
        }
        return parameterValues.distinct()
    }

    private fun filterJSON(recipientData: JSONArray, ownerName: String): JSONObject {
        val json = mutableListOf<Any>()
        val response = JSONObject()
        val filteredObjects = recipientData.filter { (it as JSONObject).optString("recipients_name") == ownerName }
        for (jsonObject in filteredObjects) {
            json.add(jsonObject)
        }
        return response.put("rider_details", json)
    }

    private fun generateParcelNo(): String {
        val regex = "(\\D*)(\\d+)".toRegex()
        val matchResult = regex.matchEntire(parcelNo)
        return  if (matchResult != null) {
            val prefix = matchResult.groups[1]?.value ?: ""
            val number = matchResult.groups[2]?.value?.toIntOrNull() ?: 0
            val formattedNumber = String.format("%06d", number+1)
            "$prefix${formattedNumber}"
        } else {
            parcelNo
        }
    }

    fun parseDate(date: String): String {
        val mnlTime = DateTimeZone.forID("Asia/Manila")
        val dt = DateTime(mnlTime).toString()
        return try {
            if (date != "") {
                if (date.length == 19) {
                    var dateResponse = date.replace(" ", "T")
                    dateResponse = "$dateResponse.000+08:00"
                    dateResponse
                } else {
                    date
                }
            } else{
                dt
            }
        } catch (e: Throwable) {
            dt
        }
    }
}