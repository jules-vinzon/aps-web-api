package app.utils.mongoDB

import app.App
import app.App.Companion.dotenv
import app.utils.Breadcrumb
import app.utils.Helpers
import app.utils.Mqtt
import org.bson.Document
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject
import org.litote.kmongo.*
import java.util.*

class Update {
    private val helpers = Helpers()
    private val fetch = Fetch()
    private val mqtt = Mqtt()
    fun updateData(
        breadcrumb: Breadcrumb, body: JSONObject, filter: Document, collection: String, identifier: String
    ): JSONObject {
        breadcrumb.log("UPDATING ${collection.uppercase(Locale.getDefault())} TABLE")
        val response = JSONObject()
        try {
            val bodyFields = body.keySet().sorted()
            val col = App.mongoDatabase.getCollection(collection)
            val query = Document("updated_at", Date())
            bodyFields.forEach {
                query.append(it, helpers.identifyKey(body, it))
            }
            val updateFieldDoc = Document("\$set", query)
            val countUpdate = col.updateOne(filter, updateFieldDoc).modifiedCount.toInt()
            breadcrumb.log("DONE UPDATING ${collection.uppercase(Locale.getDefault())} TABLE")
            if (countUpdate != 0) {
                response.put("success", true)
                response.put("message", "$identifier has been updated!")
            } else {
                response.put("success", false)
                response.put("message", "$identifier not exist!")
            }
        } catch (e: Throwable) {
            response.put("success", false)
            response.put("message", "Server error!")
            e.printStackTrace()
        }
        return response
    }

    fun approveDocument(breadcrumb: Breadcrumb, body: JSONObject, permitId: String): JSONObject {
        breadcrumb.log("START UPDATING $permitId")
        val response = JSONObject()
        val collection = "permit_request"
        try {
            val status = body.optString("status")
            val manilaTime = DateTimeZone.forID("Asia/Manila")
            val dt = DateTime(manilaTime).toString()
            val col = App.mongoDatabase.getCollection(collection)

            breadcrumb.log("CHECKING CURRENT APPROVER")
            val positionId = ObjectId(body.optString("position_id"))
            val check = col.findOne("{permit_id: '$permitId'},")
            if (check.isNullOrEmpty()) {
                response.put("success", false)
                response.put("message", "$permitId not exist!")

                breadcrumb.log("DONE CHECKING CURRENT APPROVER")
                return response
            }

            val approvers = check["approver"] as? List<Map<String, Any>> ?: emptyList()
            val currentIndex = approvers.indexOfFirst { it["position_id"] == positionId }
            val approversLength = approvers.lastIndex
            breadcrumb.log("DONE CHECKING CURRENT APPROVER")

            breadcrumb.log("START COMPOSING")
            val query = Document("updated_at", Date())
                .append("approver.\$.status", status)
                .append("approver.\$.user_id", ObjectId(body.optString("user_id")))
                .append("approver.\$.date", dt)
            if (status.uppercase() == "REJECTED" || status.uppercase() == "DELETED") {
                query.append("remarks", body.optString("remarks"))
                query.append("status", status.uppercase())
            } else {
                if (currentIndex != approversLength) {
                    val nextIndex = currentIndex + 1
                    val nextApprover = approvers[nextIndex]["position_name"].toString()
                    breadcrumb.log("NEXT APPROVER $nextApprover")
                    val approverStatus = when (nextApprover.uppercase()) {
                        dotenv["ADMIN"].toString() -> {
                            "FOR VERIFICATION"
                        }
                        dotenv["PM"].toString() -> {
                            "FOR APPROVAL"
                        }
                        dotenv["SG"].toString() -> {
                            "FOR INSPECTION"
                        } else -> {
                            "PENDING"
                        }
                    }
                    query.append("approver.\$.status", status)
                    query.append("approver.$nextIndex.status", approverStatus)
                    query.append("status", approverStatus)
                    publishMqtt(breadcrumb, approvers[nextIndex]["position_id"].toString(), body, approverStatus)
                } else {
                    query.append("approver.\$.status", "DONE")
                    query.append("status", "DONE")
                }
            }
            breadcrumb.log("DONE COMPOSING")

            val updateFieldDoc = Document("\$set", query)
            val filter = Document("permit_id", permitId).append("approver.position_id", positionId)
            val countUpdate = col.updateOne(filter, updateFieldDoc).modifiedCount.toInt()

            breadcrumb.log("DONE UPDATING ${collection.uppercase(Locale.getDefault())} TABLE")
            if (countUpdate != 0) {
                response.put("success", true)
                response.put("message", "$permitId has been updated!")
            } else {
                response.put("success", false)
                response.put("message", "$permitId not exist!")
            }
        } catch (e: Throwable) {
            response.put("success", false)
            response.put("message", "Server error!")
            e.printStackTrace()
        }
        breadcrumb.log("DONE UPDATING")
        return response
    }

    private fun publishMqtt(breadcrumb: Breadcrumb, approver: String, body: JSONObject, status: String) {
        breadcrumb.log("START PUBLISHING MQTT REQUEST")
        val secret = dotenv["SECRET_KEY"].toString()
        val topic = "$secret/$approver"
        val message = JSONObject()
        val permitId = body.optString("permit_id")
        val pdata = fetch.viewPermitRequestDetails(breadcrumb, permitId)
        val permitType = pdata.optString("permit_type")
        val unitNo = pdata.optString("unit_no")
        val userTopic = "$secret/${pdata.optString("user_id")}"

        message.put("permit_id", permitId)
        message.put("permit_type", permitType)
        message.put("current_status", status)
        message.put("unit_no", unitNo)
        message.put("read", false)
        message.put("notif_id", "TEST0002")

        mqtt.publish(breadcrumb, topic, message.toString())
        mqtt.publish(breadcrumb, userTopic, message.toString())
        breadcrumb.log("END PUBLISHING MQTT REQUEST")
    }
}