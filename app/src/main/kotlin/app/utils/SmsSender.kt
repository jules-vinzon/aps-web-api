package app.utils

import app.App
import org.bson.Document
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.ContentType.Companion.APPLICATION_FORM_URLENCODED
import org.http4k.core.body.form
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONArray
import org.json.JSONObject

class SmsSender {
    fun sendSms(details: JSONObject, status: String): Boolean {
        val smsConf = App.dotenv["SMS_CONFIG"].toString()
        val text = message(details, status)
        println(text)
        println(text.isNotEmpty())
        if (smsConf == "ON" && text.isNotEmpty()) {
            val smsUrl = App.dotenv["SMS_URL"].toString()
            val receiverNo = "63${details.optString("recipients_mobile_no").substring(1, 11)}"

            val request = Request(Method.POST, smsUrl)
                .header("Content-Type", APPLICATION_FORM_URLENCODED.toHeaderValue())
                .form("api_key", App.dotenv["API_KEY"].toString())
                .form("api_secret", App.dotenv["API_SECRET"].toString())
                .form("to", receiverNo)
                .form("text", text)
                .form("from", App.dotenv["FROM"].toString())
                .form("callback_url", App.dotenv["CALLBACK_URL"].toString())
                .form("callback_method", "POST")

            val responseReq = App.httpClient(request)
            val responseBody = JSONObject(responseReq.body.toString())
            val responseStatus = responseReq.status.toString()
            val toSave = JSONObject().put("receiverNo", receiverNo)
            toSave.put("message", text)
            toSave.put("responseBody", responseBody)
            toSave.put("responseStatus", responseStatus)
            toSave.put("text", text)
            saving(details, toSave)
            println("Message: $responseBody")
            println("Status: $responseStatus")
            return true
        } else {
            println("SMS CONFIG OFF OR NO SMS TO BE SEND")
            return false
        }
    }

    private fun message(details: JSONObject, status: String): String {
        val parcelData = details.optString("parcel_id")
        var parcelId = "";
        if (parcelData.isNotEmpty() && parcelData[0] == '[') {
            val arrayData = JSONArray(parcelData)
            for (i in 0 until arrayData.length()) {
                parcelId += arrayData[i]
                if (arrayData.length() != 1 && i != arrayData.length() - 1) {
                    parcelId += ", "
                }
            }
        } else {
            parcelId = parcelData
        }

        return when (status) {
            "new" ->
                "Dear Kroma Resident,\n" +
                "\nGood day. Your parcel has arrived at the lobby. You may claim this at the front desk. Kindly scan the QR code on the parcel and sign  it as proof of receipt. \n" +
                "\nParcel ID: $parcelId\n" +
                "\nPlease claim your parcel within 72 hrs or your parcel will be transferred to our Storage room and may cause delays on claiming."
            "received" ->
                "Dear Kroma Resident,\n" +
                "\nYour parcel/s has/have been received by ${details.optString("receiver_name")} at the lobby. Thank you!\n" +
                "\nParcel ID: $parcelId\n" +
                "\nDisclaimer: If you haven’t received your parcel personally, kindly approach or call our front desk for assistance."
            "3" ->
                "Dear Kroma Resident,\n" +
                "\nGood day. Your parcel hasn't been claimed within 72 hrs and is now in our Storage Room. Kindly approach our lobby personnel for claiming. This may take 15-20 mins.\n" +
                "\nParcel ID: $parcelId\n" +
                "\nPlease be reminded that failure to claim your parcel within 30 days will result to application of storage fee.\n"
            "30" ->
                "Dear Kroma Resident,\n" +
                "\nGood day. Your parcel hasn't been claimed within 30 days and is now subjected to Storage Fees (P500 for small and P1000 for medium sized parcels). Kindly approach our lobby personnel for claiming and pay your fees in the Admin Office." +
                "\nParcel ID: $parcelId\n"
            "60" ->
                "Dear Kroma Resident,\n" +
                "\nGood day. Your parcel hasn't been claimed within 60 days and is now subjected to Storage Fees (P1000 for small and P3000 for medium sized parcels). Kindly approach our lobby personnel for claiming and pay your fees in the Admin Office." +
                "\nParcel ID: $parcelId\n"
            "90" ->
                "Dear Kroma Resident,\n" +
                "\nGood day. Your parcel hasn't been claimed within 90 days and is now subjected to Storage Fees (P3000 for small and P5000 for medium sized parcels). Kindly approach our lobby personnel for claiming and pay your fees in the Admin Office.\n" +
                "\nParcel ID: $parcelId\n" +
                "\nPlease claim your parcel within 5 days or it will be disposed."
            "95" ->
                "Dear Kroma Resident,\n" +
                "\nGood day. Your parcel hasn't been claimed within 95 days and is now for disposal. Kindly claim your parcel within the day to avoid inconvenience." +
                "\nParcel ID: $parcelId\n"
            else -> {
                ""
            }
        }
    }


    private fun saving(details: JSONObject, toSave: JSONObject) {
        val col = App.mongoDatabase.getCollection("sms")
        val data = Document()
        val dt = DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString()
        data["parcel_id"] = details.optString("parcel_id")
        data["response_body"] = Document.parse(toSave.optString("responseBody"))
        data["response_status"] = toSave.optString("responseStatus")
        data["message"] = toSave.optString("message")
        data["is_sent"] = false
        data["created_at"] = dt
        data["updated_at"] = dt
        col.insertOne(data)
    }
}

