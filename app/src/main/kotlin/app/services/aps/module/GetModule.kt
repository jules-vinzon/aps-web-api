package app.services.aps.module

import app.utils.Breadcrumb
import app.utils.MongoDbLogs
import app.utils.mongoDB.Fetch
import org.json.JSONObject

class GetModule {
    private val dbLogs = MongoDbLogs()
    private val fetch = Fetch()
    fun getModule(breadcrumb: Breadcrumb, reqId: String): JSONObject {
        breadcrumb.log("START PROCESS GET MODULE")
        val response = JSONObject()
        val composeResponse = JSONObject()
        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", reqId, JSONObject(), "user.id", "GET MODULE")
        try {
            val moduleList = fetch.fetchArray(breadcrumb, "module", "{status: 'ACTIVE'}")
            if (moduleList.isNotEmpty()) {
                composeResponse.put("success", true)
                composeResponse.put("data", moduleList)

            } else {
                composeResponse.put("success", false)
                composeResponse.put("message", "No Module Found!")
            }
            response.put("httpStatus", 200)
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Error on fetching module!")

            response.put("HttpStatus", 502)
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", reqId, response, "user.id", "GET MODULE")
        response.put("response", composeResponse)
        return response
    }

    fun getSubModule(breadcrumb: Breadcrumb, reqId: String): JSONObject {
        breadcrumb.log("START PROCESS GET SUBMODULE")
        val response = JSONObject()
        val composeResponse = JSONObject()
        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", reqId, JSONObject(), "user.id", "GET SUBMODULE")
        try {
            val moduleList = fetch.fetchArray(breadcrumb, "submodule", "{status: 'ACTIVE'}")
            if (moduleList.isNotEmpty()) {
                composeResponse.put("success", true)
                composeResponse.put("data", moduleList)

                response.put("httpStatus", 200)
            } else {
                composeResponse.put("success", false)
                composeResponse.put("message", "No Module Found!")

                response.put("httpStatus", 400)
            }
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Error on fetching module!")

            response.put("httpStatus", 502)
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", reqId, response, "user.id", "GET SUBMODULE")
        response.put("response", composeResponse)
        return response
    }

    fun getAllModule(breadcrumb: Breadcrumb, reqId: String): JSONObject {
        breadcrumb.log("START PROCESS GET ALL MODULE")
        val response = JSONObject()
        val composeResponse = JSONObject()
        dbLogs.saveAuditlogs("REQUEST", "REQUEST BODY", reqId, JSONObject(), "user.id", "GET ALL MODULE")
        try {
            val moduleList = fetch.fetchModule(breadcrumb)
            if (moduleList.isNotEmpty()) {
                composeResponse.put("success", true)
                composeResponse.put("data", moduleList)

                response.put("httpStatus", 200)
            } else {
                composeResponse.put("success", false)
                composeResponse.put("message", "No Module Found!")

                response.put("httpStatus", 400)
            }
        } catch (e: Throwable) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Error on fetching module!")

            response.put("httpStatus", 502)
        }
        dbLogs.saveAuditlogs("RESPONSE", "RESPONSE BODY", reqId, response, "user.id", "GET ALL MODULE")
        response.put("response", composeResponse)
        return response
    }
}