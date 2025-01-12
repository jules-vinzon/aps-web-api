package app.services.parcel_logger.pickUp

import app.App
import app.utils.Breadcrumb
import app.utils.MongoDbLogs
import org.json.JSONObject
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import java.util.*


class FetchPickUp{
    private val dbLogs = MongoDbLogs()
    fun fetchPickUp(breadcrumb: Breadcrumb, pickUpId: String): JSONObject {
        val breadcrumbId = breadcrumb.log("Fetch Pick Up")
        breadcrumb.log("PICKUP ID: $pickUpId")
        dbLogs.saveToRequestResponseLogs("REQUEST", "REQUEST BODY", breadcrumbId, JSONObject().put("pickup_id", pickUpId), "pickup_logs")

        val response = JSONObject()
        val result = JSONObject()
        try{
            val data = fetchParcel(breadcrumb, pickUpId)
            if (data.isEmpty) {
                response.put("status", "FAILED")
                response.put("data", JSONObject().put("message", "FETCH PICK UP ID DOES NOT EXIST!"))
                response.put("data", JSONObject().put("pickup_id", pickUpId))
            } else {
                response.put("data", data)
                response.put("status", "SUCCESS")
            }
            result.put("HttpStatus", 200)
        } catch (e: Throwable) {
            e.printStackTrace()
            response.put("status", "FAILED")
            result.put("HttpStatus", 400)
        }
        dbLogs.saveToRequestResponseLogs("RESPONSE", "RESPONSE BODY", breadcrumbId, response, "pickup_logs")
        breadcrumb.log("Response : $response")
        result.put("response", response)
        return result
    }

    fun fetchStatus(breadcrumb: Breadcrumb, status: String): JSONObject {
        val breadcrumbId = breadcrumb.log("Fetch Pick Up")
        breadcrumb.log("PICKUP ID: $status")
        val response = JSONObject()
        val result = JSONObject()
        dbLogs.saveToRequestResponseLogs("REQUEST", "REQUEST BODY", breadcrumbId, JSONObject().put("pickup_id", status), "pickup_logs")
        try{
            val data = fetchBulkData(breadcrumb, "{status:'${status.uppercase(Locale.getDefault())}'}")
            if (data.isEmpty()) {
                response.put("status", "FAILED")
                response.put("data", JSONObject().put("message", "NO PICK UP PARCEL RECORD!"))
                response.put("data", JSONObject().put("status", status))
            } else {
                response.put("data", data)
                response.put("status", "SUCCESS")
            }
            result.put("HttpStatus", 200)
        } catch (e: Throwable) {
            e.printStackTrace()
            response.put("status", "FAILED")
            result.put("HttpStatus", 400)
        }
        breadcrumb.log("Response : $response")
        result.put("response", response)
        dbLogs.saveToRequestResponseLogs("RESPONSE", "RESPONSE BODY", breadcrumbId, response, "pickup_logs")
        return result
    }

    fun fetchDates(breadcrumb: Breadcrumb, from: String, to: String): JSONObject {
        val breadcrumbId = breadcrumb.log("Entering FETCH PARCEL Function")
        breadcrumb.log("FROM: $from")
        breadcrumb.log("TO: $to")
        val forLogs = JSONObject().put("from", from)
        forLogs.put("to", to)
        dbLogs.saveToRequestResponseLogs("REQUEST", "REQUEST BODY", breadcrumbId, forLogs, "pickup_logs")
        val response = JSONObject()
        val result = JSONObject()
        try {
            val query = "{created_at:{${MongoOperator.gte}: '${from}T00:00:00Z', ${MongoOperator.lte}: '${to}T23:59:59Z'}}"
            breadcrumb.log("query: $query")
            val data = fetchBulkData(breadcrumb, query)
            if (data.isEmpty()) {
                response.put("status", "FAILED")
                response.put("message", "NO RECORD!")
                response.put("from", from)
                response.put("to",  to)
            } else {
                response.put("data", data)
                response.put("status", "SUCCESS")
            }
            result.put("HttpStatus", 200)
        } catch (e: Throwable) {
            e.printStackTrace()
            response.put("status", "FAILED")
            result.put("HttpStatus", 400)
        }

        breadcrumb.log("Response : $response")
        result.put("response", response)
        dbLogs.saveToRequestResponseLogs("RESPONSE", "RESPONSE BODY", breadcrumbId, response, "pickup_logs")
        return result
    }

    fun fetchParcel(breadcrumb: Breadcrumb, req: String): JSONObject {
        breadcrumb.log("FETCH ON pickup_parcel TABLE")
        return try {
            val pickUpData = App.mongoDatabase.getCollection("pickup_parcel")
            val pickUpDataObject = pickUpData.findOne("{pickup_id:'${req}'}")

            breadcrumb.log("FETCHED DATA: $pickUpDataObject")
            if (pickUpDataObject != null) {
                JSONObject(pickUpDataObject)
            } else {
                JSONObject()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            breadcrumb.log("NO DATA FETCHED")
            JSONObject()
        }
    }

    fun fetchBulkData(breadcrumb: Breadcrumb, query: String): ArrayList<JSONObject> {
        breadcrumb.log("FETCH ON pickup_parcel TABLE")
        return try {
            val parcelDataArray = ArrayList<JSONObject>()
            App.mongoDatabase
                .getCollection("pickup_parcel")
                .find(query)
                .forEach {
                    parcelDataArray.add(JSONObject(it))
                }
            breadcrumb.log("FETCHED DATA: $parcelDataArray")
            parcelDataArray
        } catch (e: Throwable) {
            e.printStackTrace()
            breadcrumb.log("NO DATA FETCHED")
            ArrayList<JSONObject>()
        }
    }
}
