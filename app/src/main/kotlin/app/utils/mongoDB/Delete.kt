package app.utils.mongoDB

import app.App
import app.utils.Breadcrumb
import org.bson.Document

class Delete {
    fun deleteOne(breadcrumb: Breadcrumb, collection: String, query: String): Boolean {
        breadcrumb.log("COLLECTION $collection")
        breadcrumb.log("QUERY $query")
        return try {
            val col = App.mongoDatabase.getCollection(collection)
            col.deleteOne(Document.parse(query))
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }
}