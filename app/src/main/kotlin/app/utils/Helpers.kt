package app.utils

import app.utils.mongoDB.Fetch
import org.bson.Document
import org.bson.types.ObjectId
import org.json.JSONArray
import org.json.JSONObject
import org.litote.kmongo.MongoOperator
import java.util.*
import kotlin.collections.ArrayList

class Helpers {
    private val fetch = Fetch()
    fun validateRequest(breadcrumb: Breadcrumb, body: JSONObject, req: ArrayList<String>, action: String): JSONObject {
        val composeResponse = JSONObject()
        val response = JSONObject()
        val bodyFields = body.keySet().sorted()
        if (bodyFields.isNotEmpty()) {
            bodyFields.forEach {
                if (req.contains(it)) {
                    if (body.optString(it).isEmpty()) {
                        composeResponse.put("success", false)
                        composeResponse.put("message", "Missing $it!")

                        breadcrumb.log("INVALID ${it.uppercase(Locale.getDefault())}")

                        response.put("HttpStatus", 400)
                        response.put("response", composeResponse)
                        response.put("valid", false)
                        return response
                    }
                }
            }
        } else {
            composeResponse.put("success", false)
            composeResponse.put("message", "Missing $req!")

            response.put("HttpStatus", 400)
            response.put("response", composeResponse)
            response.put("valid", false)
            return response
        }
        return when (action) {
            "register" -> validateRegister(breadcrumb, body, "resident")
            "register_employee" -> validateRegister(breadcrumb, body, "employee")
            "add position" -> validateAddPosition(breadcrumb, body)
            "add module" -> validateAddModule(breadcrumb, body)
            "add submodule" -> validateAddSubModule(breadcrumb, body)
            "add role" -> validateAddRole(breadcrumb, body)
            "add unit" -> validateAddUnit(breadcrumb, body)
            else -> {
                response.put("valid", true)
                response
            }
        }
    }

    private fun validateRegister(breadcrumb: Breadcrumb, body: JSONObject, collection: String): JSONObject {
        val composeResponse = JSONObject()
        val response = JSONObject()
        val username = body.optString("username")
        val mobile = body.optString("mobile")
        val email = body.optString("email")
        val fname = body.optString("first_name")
        val mname = body.optString("middle_name")
        val lname = body.optString("last_name")
        val unit = body.optString("unit_no")
        val employeeNo = body.optString("employee_no")
        val fullname = "$fname ${if (mname.isNotEmpty()) "$mname " else ""}$lname"

        breadcrumb.log("START VALIDATING USER RECORD!")
        val isUserExist = fetch.fetchBoolean(breadcrumb, "auth", "{username: '${username}'}")
        if (isUserExist) {
            composeResponse.put("success", false)
            composeResponse.put("message", "Existing username!")

            breadcrumb.log("INVALID USERNAME!")

            response.put("HttpStatus", 400)
            response.put("response", composeResponse)
            response.put("valid", false)
            return response
        }
        breadcrumb.log("END VALIDATING USER RECORD!")
        if (collection == "resident") {
            breadcrumb.log("START VALIDATING RESIDENT RECORD!")
            val isResidentExist =
                fetch.fetchBoolean(breadcrumb, collection, "{full_name: '${fullname}', unit_no: '$unit'}")
            if (isResidentExist) {
                composeResponse.put("success", false)
                composeResponse.put("message", "Resident already exist in $unit!")

                breadcrumb.log("INVALID RESIDENT}")

                response.put("HttpStatus", 400)
                response.put("response", composeResponse)
                response.put("valid", false)
                return response
            }

            val isEmailNumExist = fetch.fetchBoolean(
                breadcrumb,
                collection,
                "{${MongoOperator.or}: [{mobile: '${mobile}'}, {email: '${email}'}]}"
            )
            if (isEmailNumExist) {
                composeResponse.put("success", false)
                composeResponse.put("message", "Email or Mobile already used!")

                breadcrumb.log("INVALID RESIDENT}")

                response.put("HttpStatus", 400)
                response.put("response", composeResponse)
                response.put("valid", false)
                return response
            }
            breadcrumb.log("END VALIDATING RESIDENT RECORD!")
        } else {
            breadcrumb.log("START VALIDATING RESIDENT RECORD!")
            val isEmployeeExist = fetch.fetchBoolean(
                breadcrumb,
                collection,
                "{${MongoOperator.or}: [{fullname: '$fullname'}, {employee_no: '$employeeNo'}, {mobile: '$mobile'}, {email: '$email'}]}"
            )
            if (isEmployeeExist) {
                composeResponse.put("success", false)
                composeResponse.put("message", "$employeeNo already exist!")

                breadcrumb.log("INVALID RESIDENT}")

                response.put("HttpStatus", 400)
                response.put("response", composeResponse)
                response.put("valid", false)
                return response
            }
            breadcrumb.log("END VALIDATING RESIDENT RECORD!")
        }
        response.put("valid", true)
        return response
    }

    private fun validateAddPosition(breadcrumb: Breadcrumb, body: JSONObject): JSONObject {
        val response = JSONObject()
        val composeResponse = JSONObject()
        breadcrumb.log("START VALIDATING USER RECORD!")
        val code = body.optString("position_code").lowercase(Locale.getDefault())
        val name = body.optString("position_name").uppercase(Locale.getDefault())
        val collection = "position"
        val check = fetch.fetchBoolean(
            breadcrumb,
            collection,
            "{${MongoOperator.or}: [{position_code: '${code}'}, {position_name: '${name}'}]}"
        )
        if (check) {
            composeResponse.put("message", "Position Name or Position Code already exist!")
            composeResponse.put("success", false)

            response.put("response", composeResponse)
            response.put("valid", false)
            return response
        }
        breadcrumb.log("END VALIDATING USER RECORD!")
        response.put("valid", true)
        return response
    }

    private fun validateAddModule(breadcrumb: Breadcrumb, body: JSONObject): JSONObject {
        val response = JSONObject()
        val composeResponse = JSONObject()
        breadcrumb.log("START VALIDATING USER RECORD!")
        val position = body.optString("position")
        val name = body.optString("name").uppercase(Locale.getDefault())
        val collection = "module"
        val check = fetch.fetchBoolean(
            breadcrumb,
            collection,
            "{${MongoOperator.or}: [{position: ${position}}, {name: '${name}'}]}"
        )
        if (check) {
            composeResponse.put("message", "Position or Name already exist!")
            composeResponse.put("success", false)

            response.put("response", composeResponse)
            response.put("valid", false)
            return response
        }
        breadcrumb.log("END VALIDATING USER RECORD!")
        response.put("valid", true)
        return response
    }

    private fun validateAddSubModule(breadcrumb: Breadcrumb, body: JSONObject): JSONObject {
        val response = JSONObject()
        val composeResponse = JSONObject()
        breadcrumb.log("START VALIDATING USER RECORD!")
        val parentId = body.optString("parent_id")
        val position = body.optString("position")
        val name = body.optString("name").uppercase(Locale.getDefault())
        val collection = "submodule"
        val check = fetch.fetchBoolean(
            breadcrumb,
            collection,
            "{${MongoOperator.or}: [{position: '${position}'}, {name: '${name}'}], parent_id: ObjectId('$parentId')}"
        )
        if (check) {
            composeResponse.put("message", "Position or Name already exist!")
            composeResponse.put("success", false)

            response.put("response", composeResponse)
            response.put("valid", false)
            return response
        }
        breadcrumb.log("END VALIDATING USER RECORD!")
        response.put("valid", true)
        return response
    }

    private fun validateAddRole(breadcrumb: Breadcrumb, body: JSONObject): JSONObject {
        val response = JSONObject()
        val composeResponse = JSONObject()
        breadcrumb.log("START VALIDATING ROLE RECORD!")
        val role = body.optString("role").uppercase(Locale.getDefault())
        val collection = "role"
        val check = fetch.fetchBoolean(breadcrumb, collection, "{role: '$role'}")
        if (check) {
            composeResponse.put("message", "Role already exist!")
            composeResponse.put("success", false)

            response.put("response", composeResponse)
            response.put("valid", false)
            return response
        }
        breadcrumb.log("END VALIDATING ROLE RECORD!")
        response.put("valid", true)
        return response
    }

    private fun validateAddUnit(breadcrumb: Breadcrumb, body: JSONObject): JSONObject {
        val response = JSONObject()
        val composeResponse = JSONObject()
        breadcrumb.log("START VALIDATING UNITS RECORD!")
        val unit = body.optString("unit_no").uppercase(Locale.getDefault())
        val collection = "units"
        val check = fetch.fetchBoolean(breadcrumb, collection, "{unit_no: '$unit'}")
        if (check) {
            composeResponse.put("message", "Unit already exist!")
            composeResponse.put("success", false)

            response.put("response", composeResponse)
            response.put("valid", false)
            return response
        }
        breadcrumb.log("END VALIDATING UNITS RECORD!")
        response.put("valid", true)
        return response
    }

    fun identifyKey(body: JSONObject, params: String): Any {
        return when (body.get(params)) {
            is String -> when (params) {
                "user_id", "permit_type", "position_id" -> ObjectId(body.optString(params))
                else -> body.optString(params)
            }
            is Double -> body.optDouble(params)
            is ObjectId -> ObjectId(body.optString(params))
            is Int -> body.optInt(params)
            is Long -> body.optLong(params)
            is Float -> body.optFloat(params)
            is Boolean -> body.optBoolean(params)
            is JSONObject -> Document.parse(body.optString(params))
            is JSONArray -> parseArray(body.optJSONArray(params))
            else -> {
                body.optString(params)
            }
        }
    }

    private fun parseArray(arr: JSONArray): ArrayList<Document> {
        val arrayResponse = ArrayList<Document>()
        arr.forEach { item ->
            val jsonCal = JSONObject(item.toString())
            val document = Document()
            jsonCal.keySet().forEach { key ->
                document[key] = identifyKey(jsonCal, key)
            }
            arrayResponse.add(document)
        }
        return arrayResponse
    }
}