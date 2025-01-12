package app.services.parcel_logger.sms

import app.App
import app.utils.Breadcrumb
import io.javalin.http.Context
import org.json.JSONObject
import org.litote.kmongo.find
import java.util.ArrayList

class CallbackService {
    fun callback(breadcrumb: Breadcrumb, context: Context): JSONObject {
        breadcrumb.log("REQUEST: $context")
        breadcrumb.log("REQUEST BODY: ${context.formParamMap()}")
        val messageId = context.formParam("message_id")
        breadcrumb.log("MESSAGE ID: $messageId")

        val smsRec = fetchSms(breadcrumb, "{'response_body.phone_number_list.message_id': '$messageId'}")
        breadcrumb.log("SMS RECORD: $smsRec")

        return JSONObject()
    }

    private fun fetchSms(breadcrumb: Breadcrumb, query: String): ArrayList<JSONObject> {
        breadcrumb.log("FETCH ON SMS TABLE")
        return try {
            val smsData = ArrayList<JSONObject>()
            App.mongoDatabase
                .getCollection("sms")
                .find(query)
                .forEach {
                    smsData.add(JSONObject(it))
                }
            breadcrumb.log("FETCHED DATA: $smsData")
            smsData
        } catch (e: Throwable) {
            e.printStackTrace()
            breadcrumb.log("NO DATA FETCHED")
            ArrayList<JSONObject>()
        }
    }
}