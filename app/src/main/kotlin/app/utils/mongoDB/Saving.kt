package app.utils.mongoDB

import app.App
import app.utils.Breadcrumb
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*
import org.bson.Document

class Saving {
    fun save(breadcrumb: Breadcrumb, data: Document, collection: String): Boolean {
        breadcrumb.log("FETCHING ${collection.uppercase(Locale.getDefault())}")
        return try {
            val col = App.mongoDatabase.getCollection(collection)
            data["created_at"] = Date()
            data["updated_at"] = Date()
            col.insertOne(data)
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            breadcrumb.log("ERROR ON FETCHING ${collection.uppercase(Locale.getDefault())}")
            false
        }
    }

    fun saveObjectId(breadcrumb: Breadcrumb, data: Document, collection: String): String {
        breadcrumb.log("FETCHING ${collection.uppercase(Locale.getDefault())}")
        return try {
            val col = App.mongoDatabase.getCollection(collection)
            data["created_at"] = Date()
            data["updated_at"] = Date()
            return col.insertOne(data).insertedId?.asObjectId()?.value.toString()
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING ${collection.uppercase(Locale.getDefault())}")
            ""
        }
    }
}