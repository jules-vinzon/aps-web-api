package app.utils

import app.App.Companion.mongoDatabase
import org.bson.Document
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject

class MongoDbLogs {
    fun saveToRequestResponseLogs(type: String, description: String, breadcrumbid: String, req: JSONObject, collection: String): String {
        try {
            val dateTimeNow = DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString()

            val payload = Document()

            payload["type"] = type
            payload["description"] = description
            payload["breadcrumb_id"] = breadcrumbid
            payload["payload"] = Document.parse(req.toString())
            payload["header"] = Document.parse(req.toString())
            payload["created_at"] = dateTimeNow
            payload["updated_at"] = dateTimeNow

            val col = mongoDatabase.getCollection(collection)
            col.insertOne(payload)

            return "true"

        } catch (e: Throwable) {
            e.printStackTrace()
            return "false"
        }
    }

    fun saveAuditlogs(type: String, description: String, breadcrumbid: String, req: JSONObject, userId: String, actionTaken: String) {
        try {
            val dateTimeNow = DateTime().toDateTime(DateTimeZone.forID("Asia/Manila")).toString()

            val payload = Document()

            payload["type"] = type
            payload["description"] = description
            payload["breadcrumb_id"] = breadcrumbid
            payload["payload"] = Document.parse(req.toString())
            payload["user"] = userId
            payload["action"] = actionTaken
            payload["created_at"] = dateTimeNow
            payload["updated_at"] = dateTimeNow

//            val col = mongoDatabase.getCollection("aps_auditlogs")
//            col.insertOne(payload)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}

