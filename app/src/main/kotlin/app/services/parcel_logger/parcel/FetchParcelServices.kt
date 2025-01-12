package app.services.parcel_logger.parcel

import app.App
import app.services.parcel_logger.pickUp.FetchPickUp
import org.json.JSONObject
import app.utils.Breadcrumb
import org.litote.kmongo.*
import java.time.ZonedDateTime
import java.util.ArrayList

class FetchParcelServices {
    private val pickUp = FetchPickUp()
    fun fetchParcel(breadcrumb: Breadcrumb, parcelId: String): JSONObject {
        breadcrumb.log("Entering ADD PARCEL Function")
        breadcrumb.log("PARCEL ID: $parcelId")
        val response = JSONObject()
        val result = JSONObject()
        try {
            val data = getParcel(breadcrumb, parcelId)
            if (data.isEmpty) {
                response.put("status", "FAILED")
                response.put("data", JSONObject().put("message", "PARCEL ID DOES NOT EXIST!"))
                response.put("data", JSONObject().put("parcel_id", parcelId))
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
        return result
    }

    fun getParcel(breadcrumb: Breadcrumb, parcelId: String): JSONObject {
        breadcrumb.log("FETCH ON PARCEL TABLE")
        return try {
            val parcelData = App.mongoDatabase.getCollection("parcel")
            val parcelDataObject = parcelData.findOne("{parcel_id:'${parcelId}'}")
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

    fun fetchAllParcel(breadcrumb: Breadcrumb, from: String, to: String): JSONObject {
        breadcrumb.log("Entering FETCH PARCEL Function")
        breadcrumb.log("FROM: $from")
        breadcrumb.log("TO: $to")
        val response = JSONObject()
        val result = JSONObject()
        try {
            val data = fetchBulkData(breadcrumb, "{concierge_name: { ${MongoOperator.ne}: '' },  created_at:{${MongoOperator.gte}: '${from}T00:00:00Z', ${MongoOperator.lte}: '${to}T23:59:59Z'}}")
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
        return result
    }

    fun updateListParcel(breadcrumb: Breadcrumb): JSONObject {
        breadcrumb.log("Entering FETCH PARCEL Function")
        val breadcrumb = Breadcrumb()
        breadcrumb.log("SCHEDULED ALL PARCEL SERVICE TRIGGERED")
        try {
            App.dateStart = App.dateEnd.minusDays(1)
            App.dateEnd = ZonedDateTime.now(App.zone).plusDays(1)
            breadcrumb.log("DATE START ${App.dateStart}")
            breadcrumb.log("DATE END ${App.dateEnd}")
        } catch (e: Exception) {
            println("Error accessing the database: ${e.message}")
        } finally {
           fetchBulkData(breadcrumb, "{concierge_name: { ${MongoOperator.ne}: '' },  created_at:{${MongoOperator.gte}: '${App.dateStart}', ${MongoOperator.lte}: '${App.dateEnd}'}}")
               .forEach{
                   App.allParcel.add(it)
               }
            val query = "{created_at:{${MongoOperator.gte}: '${App.dateStart}', ${MongoOperator.lte}: '${App.dateEnd}'}}"
            pickUp.fetchBulkData(breadcrumb, query)
                .forEach {
                    App.allPickUp.add(it)
                }
        }
        return JSONObject()
    }

    fun fetchByStatus(breadcrumb: Breadcrumb, parcelID: String): JSONObject {
        breadcrumb.log("Entering ADD PARCEL Function")
        breadcrumb.log("PARCEL ID: $parcelID")
        val response = JSONObject()
        val result = JSONObject()
        try {
            val data = fetchAllparcelID(breadcrumb, parcelID)
            if (data.isEmpty()) {
                val parcelDataArray = ArrayList<JSONObject>()
                parcelDataArray.add(JSONObject().put("message", "PARCEL ID DOES NOT EXIST!"))
                breadcrumb.log("RESPONSE $parcelDataArray")
                response.put("status", "FAILED")
                response.put("data", parcelDataArray)
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
        return result
    }

    private fun fetchAllparcelID(breadcrumb: Breadcrumb, parcelId: String): ArrayList<JSONObject> {
        breadcrumb.log("FETCH ON PARCEL TABLE")
        return try {
            val parcelDataArray = ArrayList<JSONObject>()
            val unitNo = JSONObject(App.mongoDatabase
                .getCollection("parcel")
                .find("{parcel_id:'$parcelId'}")
                .projection(excludeId())
                .first()).optString("recipients_unit_number")
            breadcrumb.log("GET UNIT NO: $unitNo")
            if (unitNo != "") {
                App.mongoDatabase
                    .getCollection("parcel")
                    .find("{parcel_status:'PENDING', recipients_unit_number: '$unitNo' concierge_name: { ${MongoOperator.ne}: '' }}")
                    .projection(excludeId())
                    .forEach {
                        parcelDataArray.add(JSONObject(it))
                    }
            }
            breadcrumb.log("FETCHED DATA: $parcelDataArray")
            parcelDataArray
        } catch (e: Throwable) {
            e.printStackTrace()
            breadcrumb.log("NO DATA FETCHED")
            ArrayList<JSONObject>()
        }
    }

    fun fetchByRiderQr(breadcrumb: Breadcrumb, qr: String): JSONObject {
        breadcrumb.log("Entering FETCH BY RIDER QR Function")
        breadcrumb.log("STATUS: $qr")
        val response = JSONObject()
        val result = JSONObject()
        try {
            val data = fetchBulkData(breadcrumb, "{rider_qr_details:'$qr'}")
            if (data.isEmpty()) {
                response.put("status", "FAILED")
                response.put("data", JSONObject().put("message", "RIDER QR DOES NOT EXIST!"))
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
        return result
    }

    fun fetchBulkData(breadcrumb: Breadcrumb, query: String): ArrayList<JSONObject> {
        breadcrumb.log("FETCH ON PARCEL TABLE")
        return try {
            val parcelDataArray = ArrayList<JSONObject>()
                App.mongoDatabase
                    .getCollection("parcel")
                    .find(query)
                    .projection(excludeId())
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