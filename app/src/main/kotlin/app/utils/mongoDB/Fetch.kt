package app.utils.mongoDB

import app.App
import app.utils.Breadcrumb
import org.bson.Document
import org.bson.types.ObjectId
import org.json.JSONArray
import org.json.JSONObject
import org.litote.kmongo.find
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class Fetch {
    fun fetchArray(breadcrumb: Breadcrumb, collection: String, query: String): ArrayList<JSONObject> {
        breadcrumb.log("FETCHING ${collection.uppercase(Locale.getDefault())}")
        breadcrumb.log("QUERY $query")
        val arrayResponse = ArrayList<JSONObject>()
        try {
            val col = App.mongoDatabase.getCollection(collection)
            if (query.isEmpty()) {
                col.find().projection(Document("_id", 0L).append("created_at", 0L).append("updated_at", 0L).append("status", 0L)).forEach { docs ->
                    arrayResponse.add(JSONObject(docs))
                }
            } else {
                col.find(query).projection(Document("_id", 0L).append("created_at", 0L).append("updated_at", 0L).append("status", 0L)).forEach { docs ->
                    arrayResponse.add(JSONObject(docs))
                }
            }
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING ${collection.uppercase(Locale.getDefault())}")
            e.printStackTrace()
        }
        return arrayResponse
    }

    fun fetchJson(breadcrumb: Breadcrumb, collection: String, query: String): JSONObject {
        breadcrumb.log("FETCHING ${collection.uppercase(Locale.getDefault())}")
        breadcrumb.log("QUERY $query")
        val json = JSONObject()
        try {
            val col = App.mongoDatabase.getCollection(collection)
            if (query.isEmpty()) {
                val queryRes = col.find().firstOrNull()
                if (!queryRes.isNullOrEmpty()) {
                    queryRes.keys.filter { it !in listOf("created_at", "updated_at", "_id", "status") }.sorted()
                        .forEach { key ->
                            json.put(key, queryRes[key])
                        }

                    json.put("id", queryRes["_id"].toString())
                }
            } else {
                val queryRes = col.find(query).firstOrNull()
                if (!queryRes.isNullOrEmpty()) {
                    queryRes.keys.filter { it !in listOf("created_at", "updated_at", "_id", "status") }.sorted()
                        .forEach { key ->
                            json.put(key, queryRes[key])
                        }

                    json.put("id", queryRes["_id"].toString())
                }
            }
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING ${collection.uppercase(Locale.getDefault())}")
            e.printStackTrace()
        }
        return json
    }

    fun fetchBoolean(breadcrumb: Breadcrumb, collection: String, query: String): Boolean {
        breadcrumb.log("FETCHING ${collection.uppercase(Locale.getDefault())}")
        breadcrumb.log("QUERY $query")
        try {
            val col = App.mongoDatabase.getCollection(collection)
            return if (query.isEmpty()) {
                val queryRes = col.find().projection(Document("_id", 1L)).firstOrNull()
                !queryRes.isNullOrEmpty()
            } else {
                val queryRes = col.find(query).projection(Document("_id", 1L)).firstOrNull()
                !queryRes.isNullOrEmpty()
            }
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING ${collection.uppercase(Locale.getDefault())}")
        }
        return false
    }

    fun fetchModule(breadcrumb: Breadcrumb): ArrayList<JSONObject> {
        breadcrumb.log("START FETCHING MODULE")
        val arrayResponse = ArrayList<JSONObject>()
        try {
            val col = App.mongoDatabase.getCollection("module")
            val lookupPipeline = col.aggregate(
                listOf(
                    Document(
                        "\$match", Document("status", "ACTIVE")
                    ), Document(
                        "\$sort", Document("position", 1L)
                    ), Document(
                        "\$lookup", Document("from", "submodule").append(
                            "let", Document("parent_id", "\$_id")
                        ).append("as", "submodules").append(
                            "pipeline", listOf(
                                Document(
                                    "\$match", Document(
                                        "\$expr", Document(
                                            "\$and", listOf(
                                                Document("\$eq", listOf("\$parent_id", "$\$parent_id")),
                                                Document("\$eq", listOf("\$status", "ACTIVE"))
                                            )
                                        )
                                    )
                                ), Document(
                                    "\$sort", Document("position", 1L)
                                )
                            )
                        )
                    ), Document(
                        "\$project",
                        Document("_id", 0L).append("icon", 1L).append("name", 1L).append("position", 1L).append(
                            "submodules", Document(
                                "\$map", Document("input", "\$submodules").append("as", "submodule").append(
                                    "in",
                                    Document("icon", "$\$submodule.icon").append("name", "$\$submodule.name")
                                        .append("position", "$\$submodule.position")
                                )
                            )
                        )
                    )
                )
            ).toList()
            lookupPipeline.forEach {
                arrayResponse.add(JSONObject(it))
            }
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING MODULE")
            e.printStackTrace()
        }
        breadcrumb.log("END FETCHING MODULE")
        return arrayResponse
    }

    fun login(breadcrumb: Breadcrumb, username: String, password: String): JSONObject {
        breadcrumb.log("START FETCHING AUTH")
        try {
            val col = App.mongoDatabase.getCollection("auth")
            val lookupPipeline = col.aggregate(
                listOf(
                    Document(
                        "\$match", Document("username", username).append("password", password)
                    ), Document(
                        "\$lookup",
                        Document("from", "resident").append("localField", "_id").append("foreignField", "auth_id")
                            .append("as", "user_details")
                    ), Document(
                        "\$unwind", Document("path", "\$user_details").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$lookup",
                        Document("from", "employee").append("localField", "_id").append("foreignField", "auth_id")
                            .append("as", "employee_details")
                    ), Document(
                        "\$unwind", Document("path", "\$employee_details").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$addFields", Document(
                            "user_details", Document("\$ifNull", listOf("\$user_details", "\$employee_details"))
                        )
                    ), Document(
                        "\$lookup",
                        Document("from", "position").append("localField", "user_details.position_id")
                            .append("foreignField", "_id").append("as", "position")
                    ), Document(
                        "\$unwind", Document("path", "\$position").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$lookup",
                        Document("from", "role").append("localField", "user_details.role_id")
                            .append("foreignField", "_id").append("as", "role_data")
                    ), Document(
                        "\$unwind", Document("path", "\$role_data").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$unwind", Document("path", "\$role_data.modules").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$lookup",
                        Document("from", "module").append("localField", "role_data.modules.module_id")
                            .append("foreignField", "_id").append("as", "modules_data")
                    ), Document(
                        "\$unwind", Document("path", "\$modules_data").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$lookup",
                        Document("from", "submodule").append("localField", "role_data.modules.submodules.module_id")
                            .append("foreignField", "_id").append("as", "submodules_data")
                    ), Document(
                        "\$addFields", Document(
                            "submodules", Document(
                                "\$map", Document(
                                    "input", Document(
                                        "\$ifNull", listOf("\$role_data.modules.submodules", listOf<String>())
                                    )
                                ).append("as", "submodule").append(
                                    "in", Document("action", "$\$submodule.action").append(
                                        "submodule_data", Document(
                                            "\$let", Document(
                                                "vars", Document(
                                                    "matchedSubmoduleData", Document(
                                                        "\$arrayElemAt", listOf(
                                                            "\$submodules_data", Document(
                                                                "\$indexOfArray", listOf(
                                                                    "\$submodules_data._id", "$\$submodule.module_id"
                                                                )
                                                            )
                                                        )
                                                    )
                                                )
                                            ).append(
                                                "in", Document(
                                                    "\$mergeObjects", listOf(
                                                        Document(
                                                            "icon", "$\$matchedSubmoduleData.icon"
                                                        ), Document(
                                                            "name", "$\$matchedSubmoduleData.name"
                                                        ), Document(
                                                            "position", "$\$matchedSubmoduleData.position"
                                                        ), Document(
                                                            "module_id",
                                                            Document("\$toString", "$\$matchedSubmoduleData._id")
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    ), Document(
                        "\$lookup", Document("from", "permit").append(
                            "pipeline", listOf(
                                Document(
                                    "\$match", Document(
                                        "\$expr", Document("\$ne", listOf("status", "ACTIVE"))
                                    )
                                )
                            )
                        ).append("as", "permits")
                    ), Document(
                        "\$unwind", Document("path", "\$permits").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$group", Document("_id", "\$_id").append(
                            "username", Document("\$first", "\$username")
                        ).append(
                            "account_status", Document("\$first", "\$account_status")
                        ).append(
                            "position_name", Document("\$first", "\$position.position_name")
                        ).append("position_id", Document("\$first", Document("\$toString", "\$position._id"))).append(
                            "user_details", Document(
                                "\$mergeObjects", Document(
                                    "id", Document("\$toString", "\$user_details._id")
                                ).append("first_name", "\$user_details.first_name")
                                    .append("last_name", "\$user_details.last_name")
                                    .append("middle_name", "\$user_details.middle_name")
                                    .append("full_name", "\$user_details.full_name")
                                    .append("nickname", "\$user_details.nickname")
                                    .append("unit_no", "\$user_details.unit_no")
                                    .append("mobile", "\$user_details.mobile").append("email", "\$user_details.email")
                            )
                        ).append(
                            "permit", Document(
                                "\$addToSet", Document(
                                    "\$mergeObjects", Document(
                                        "id", Document("\$toString", "\$permits._id")
                                    ).append("permit", "\$permits.permit")
                                )
                            )
                        ).append(
                            "role_details", Document(
                                "\$addToSet", Document("action", "\$role_data.modules.action").append(
                                    "position", "\$modules_data.position"
                                ).append(
                                    "modules_data", Document(
                                        "\$let", Document(
                                            "vars", Document(
                                                "cleanModule", Document(
                                                    "\$mergeObjects", listOf(
                                                        Document(
                                                            "module_id", Document("\$toString", "\$modules_data._id")
                                                        ),
                                                        Document("icon", "\$modules_data.icon"),
                                                        Document("name", "\$modules_data.name"),
                                                        Document("position", "\$modules_data.position")
                                                    )
                                                )
                                            )
                                        ).append("in", "$\$cleanModule")
                                    )
                                ).append(
                                    "submodules", Document(
                                        "\$sortArray", Document("input", "\$submodules").append(
                                            "sortBy", Document("submodule_data.position", 1L)
                                        )
                                    )
                                )
                            )
                        )
                    ), Document(
                        "\$addFields", Document(
                            "role_details", Document(
                                "\$sortArray", Document("input", "\$role_details").append(
                                    "sortBy", Document("position", 1L)
                                )
                            )
                        )
                    )
                )
            ).toList()
            lookupPipeline.forEach { docs ->
                val json = JSONObject()
                docs.keys.filter { it !in listOf("created_at", "updated_at", "_id") }.sorted().forEach { key ->
                    json.put(key, docs[key])
                }
                json.put("id", docs["_id"].toString())
                return json
            }
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING MODULE")
            e.printStackTrace()
        }
        breadcrumb.log("END FETCHING MODULE")
        return JSONObject()
    }

    fun getUserRole(breadcrumb: Breadcrumb, token: String): JSONObject {
        breadcrumb.log("START FETCHING AUTH")
        try {
            val col = App.mongoDatabase.getCollection("active_token")
            val lookupPipeline = col.aggregate(
                listOf(
                    Document(
                        "\$match", Document(
                            "token", token
                        )
                    ), Document(
                        "\$lookup",
                        Document("from", "auth").append("localField", "user_id").append("foreignField", "_id")
                            .append("as", "auth")
                    ), Document(
                        "\$unwind", Document("path", "\$auth").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$lookup",
                        Document("from", "resident").append("localField", "auth._id").append("foreignField", "auth_id")
                            .append("as", "user_details")
                    ), Document(
                        "\$unwind", Document("path", "\$user_details").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$lookup",
                        Document("from", "employee").append("localField", "auth._id").append("foreignField", "auth_id")
                            .append("as", "employee_details")
                    ), Document(
                        "\$unwind", Document("path", "\$employee_details").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$addFields", Document(
                            "user_details", Document("\$ifNull", listOf("\$user_details", "\$employee_details"))
                        )
                    ), Document(
                        "\$lookup",
                        Document("from", "position").append("localField", "user_details.position_id")
                            .append("foreignField", "_id").append("as", "position")
                    ), Document(
                        "\$unwind", Document("path", "\$position").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$lookup",
                        Document("from", "role").append("localField", "user_details.role_id")
                            .append("foreignField", "_id").append("as", "role_data")
                    ), Document(
                        "\$unwind", Document("path", "\$role_data").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$unwind", Document("path", "\$role_data.modules").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$lookup",
                        Document("from", "module").append("localField", "role_data.modules.module_id")
                            .append("foreignField", "_id").append("as", "modules_data")
                    ), Document(
                        "\$unwind", Document("path", "\$modules_data").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$lookup",
                        Document("from", "submodule").append("localField", "role_data.modules.submodules.module_id")
                            .append("foreignField", "_id").append("as", "submodules_data")
                    ), Document(
                        "\$addFields", Document(
                            "submodules", Document(
                                "\$map", Document(
                                    "input", Document(
                                        "\$ifNull", listOf("\$role_data.modules.submodules", listOf<String>())
                                    )
                                ).append("as", "submodule").append(
                                    "in", Document("action", "$\$submodule.action").append(
                                        "submodule_data", Document(
                                            "\$let", Document(
                                                "vars", Document(
                                                    "matchedSubmoduleData", Document(
                                                        "\$arrayElemAt", listOf(
                                                            "\$submodules_data", Document(
                                                                "\$indexOfArray", listOf(
                                                                    "\$submodules_data._id", "$\$submodule.module_id"
                                                                )
                                                            )
                                                        )
                                                    )
                                                )
                                            ).append(
                                                "in", Document(
                                                    "\$mergeObjects", listOf(
                                                        Document(
                                                            "icon", "$\$matchedSubmoduleData.icon"
                                                        ), Document(
                                                            "name", "$\$matchedSubmoduleData.name"
                                                        ), Document(
                                                            "position", "$\$matchedSubmoduleData.position"
                                                        ), Document(
                                                            "module_id",
                                                            Document("\$toString", "$\$matchedSubmoduleData._id")
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    ), Document(
                        "\$lookup", Document("from", "permit").append(
                            "pipeline", listOf(
                                Document(
                                    "\$match", Document(
                                        "\$expr", Document("\$ne", listOf("status", "ACTIVE"))
                                    )
                                )
                            )
                        ).append("as", "permits")
                    ), Document(
                        "\$unwind", Document("path", "\$permits").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$group", Document("_id", "\$_id").append(
                            "username", Document("\$first", "\$auth.username")
                        ).append(
                            "account_status", Document("\$first", "\$auth.account_status")
                        ).append(
                            "position_name", Document("\$first", "\$position.position_name")
                        ).append("position_id", Document("\$first", Document("\$toString", "\$position._id"))).append(
                            "user_details", Document(
                                "\$mergeObjects", Document(
                                    "id", Document("\$toString", "\$user_details._id")
                                ).append("first_name", "\$user_details.first_name")
                                    .append("last_name", "\$user_details.last_name")
                                    .append("middle_name", "\$user_details.middle_name")
                                    .append("full_name", "\$user_details.full_name")
                                    .append("nickname", "\$user_details.nickname")
                                    .append("unit_no", "\$user_details.unit_no")
                                    .append("mobile", "\$user_details.mobile").append("email", "\$user_details.email")
                            )
                        ).append(
                            "permit", Document(
                                "\$addToSet", Document(
                                    "\$mergeObjects", Document(
                                        "id", Document("\$toString", "\$permits._id")
                                    ).append("permit", "\$permits.permit")
                                )
                            )
                        ).append(
                            "role_details", Document(
                                "\$addToSet", Document("action", "\$role_data.modules.action").append(
                                    "position", "\$modules_data.position"
                                ).append(
                                    "modules_data", Document(
                                        "\$let", Document(
                                            "vars", Document(
                                                "cleanModule", Document(
                                                    "\$mergeObjects", listOf(
                                                        Document(
                                                            "module_id", Document("\$toString", "\$modules_data._id")
                                                        ),
                                                        Document("icon", "\$modules_data.icon"),
                                                        Document("name", "\$modules_data.name"),
                                                        Document("position", "\$modules_data.position")
                                                    )
                                                )
                                            )
                                        ).append("in", "$\$cleanModule")
                                    )
                                ).append(
                                    "submodules", Document(
                                        "\$sortArray", Document("input", "\$submodules").append(
                                            "sortBy", Document("submodule_data.position", 1L)
                                        )
                                    )
                                )
                            )
                        )
                    ), Document(
                        "\$addFields", Document(
                            "role_details", Document(
                                "\$sortArray", Document("input", "\$role_details").append(
                                    "sortBy", Document("position", 1L)
                                )
                            )
                        )
                    )
                )
            ).toList()
            lookupPipeline.forEach { docs ->
                val json = JSONObject()
                docs.keys.filter { it !in listOf("created_at", "updated_at", "_id") }.sorted().forEach { key ->
                    json.put(key, docs[key])
                }
                json.put("id", docs["_id"].toString())
                return json
            }
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING MODULE")
            e.printStackTrace()
        }
        breadcrumb.log("END FETCHING MODULE")
        return JSONObject()
    }

    fun fetchRole(breadcrumb: Breadcrumb): JSONObject {
        breadcrumb.log("START FETCHING AUTH")
        val collection = "role"
        try {
            val col = App.mongoDatabase.getCollection(collection)
            val queryRes = col.find("{status: 'ACTIVE'}").firstOrNull()
            val Json = JSONObject(queryRes)
            println(Json)
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING MODULE")
            e.printStackTrace()
        }
        breadcrumb.log("END FETCHING MODULE")
        return JSONObject()
    }

    fun fetchPermits(breadcrumb: Breadcrumb): ArrayList<JSONObject> {
        breadcrumb.log("START FETCHING AUTH")
        val arrayResponse = ArrayList<JSONObject>()
        try {
            val col = App.mongoDatabase.getCollection("permit")
            val lookupPipeline = col.aggregate(
                listOf(
                    Document(
                        "\$match", Document("status", "ACTIVE")
                    ), Document("\$unwind", "\$approver"), Document(
                        "\$lookup",
                        Document("from", "position").append("localField", "approver.position_id")
                            .append("foreignField", "_id").append("as", "positionData")
                    ), Document("\$unwind", "\$positionData"), Document(
                        "\$addFields", Document(
                            "approver", Document(
                                "position_id", Document("\$toString", "\$positionData._id")
                            ).append("position_code", "\$positionData.position_code")
                                .append("position_name", "\$positionData.position_name")
                        )
                    ), Document(
                        "\$group", Document("_id", "\$_id").append(
                            "permit", Document("\$first", "\$permit")
                        ).append(
                            "status", Document("\$first", "\$status")
                        ).append(
                            "approver", Document("\$push", "\$approver")
                        )
                    ), Document(
                        "\$addFields", Document(
                            "approver", Document(
                                "\$sortArray", Document("input", "\$approver").append(
                                    "sortBy", Document("rank", 1L)
                                )
                            )
                        )
                    )
                )
            ).toList()
            lookupPipeline.forEach { docs ->
                val json = JSONObject()
                docs.keys.filter { it !in listOf("created_at", "updated_at", "_id") }.sorted().forEach { key ->
                    json.put(key, docs[key])
                }

                json.put("id", docs["_id"].toString())
                arrayResponse.add(json)
            }
            return arrayResponse
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING MODULE")
            e.printStackTrace()
        }
        breadcrumb.log("END FETCHING MODULE")
        return ArrayList()
    }

    fun fetchRequestPermit(breadcrumb: Breadcrumb, unitNo: String): ArrayList<JSONObject> {
        breadcrumb.log("START FETCHING REQUESTED PERMIT")
        val arrayResponse = ArrayList<JSONObject>()
        try {
            val col = App.mongoDatabase.getCollection("permit_request")
            val lookupPipeline = col.aggregate(
                listOf(
                    Document(
                        "\$match", Document(
                            "\$and", listOf(Document("unit_no", unitNo).append("status", Document("\$ne", "DELETED")))
                        )
                    ), Document(
                        "\$addFields", Document(
                            "converted", Document(
                                "\$dateToString",
                                Document("date", "\$created_at").append("format", "%Y-%m-%dT%H:%M:%S.%L+08:00")
                                    .append("timezone", "Asia/Manila")
                            )
                        )
                    ), Document(
                        "\$lookup",
                        Document("from", "permit").append("localField", "permit_type").append("foreignField", "_id")
                            .append("as", "permit")
                    ), Document(
                        "\$unwind", Document("path", "\$permit").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$project",
                        Document("permit_id", 1L).append("_id", 0L).append("status", 1L)
                            .append("date_filed", "\$converted").append("type", "\$permit.permit").append("unit_no", 1L)
                    )
                )
            ).toList()
            lookupPipeline.forEach { docs ->
                arrayResponse.add(JSONObject(docs))
            }
            return arrayResponse
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING MODULE")
            e.printStackTrace()
        }
        breadcrumb.log("END FETCHING MODULE")
        return ArrayList()
    }

    fun fetchAllRequestPermit(breadcrumb: Breadcrumb): ArrayList<JSONObject> {
        breadcrumb.log("START FETCHING REQUESTED PERMIT")
        val arrayResponse = ArrayList<JSONObject>()
        try {
            val col = App.mongoDatabase.getCollection("permit_request")
            val lookupPipeline = col.aggregate(
                listOf(
                    Document("\$match", Document("status", Document("\$ne", "DELETED"))), Document(
                        "\$lookup",
                        Document("from", "permit").append("localField", "permit_type").append("foreignField", "_id")
                            .append("as", "permit")
                    ), Document(
                        "\$addFields", Document(
                            "converted", Document(
                                "\$dateToString",
                                Document("date", "\$created_at").append("format", "%Y-%m-%dT%H:%M:%S.%L+08:00")
                                    .append("timezone", "Asia/Manila")
                            )
                        )
                    ), Document(
                        "\$unwind", Document("path", "\$permit").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$project",
                        Document("permit_id", 1L).append("_id", 0L).append("status", 1L)
                            .append("date_filed", "\$converted").append("type", "\$permit.permit").append("unit_no", 1L)
                    )
                )
            ).toList()
            lookupPipeline.forEach { docs ->
                arrayResponse.add(JSONObject(docs))
            }
            return arrayResponse
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING MODULE")
            e.printStackTrace()
        }
        breadcrumb.log("END FETCHING MODULE")
        return ArrayList()
    }

    fun filterPermit(breadcrumb: Breadcrumb, body: JSONObject): ArrayList<JSONObject> {
        breadcrumb.log("START FETCHING REQUESTED PERMIT")
        try {
            val arrayResponse = ArrayList<JSONObject>()
            val col = App.mongoDatabase.getCollection("permit_request")

            val permitId = body.optString("permit_id")
            val unitNo = body.optJSONArray("unit_no")
            val permitType = body.optJSONArray("permit_type")
            val start = body.optString("start")
            val end = body.optString("end")
            val status = body.optString("status")

            var default = listOf(
                Document(
                    "status", Document("\$regex", ".*$status")
                ), Document(
                    "permit_id", Document("\$regex", ".*$permitId")
                ), Document(
                    "status", Document("\$ne", "DELETED")
                )
            )

            if (start.isNotEmpty()) {
                val fromDate = convertDateToISO(start)
                val toDate = convertDateToISO(end)
                default = default + Document(
                    "converted", Document(
                        "\$gte", fromDate
                    ).append(
                        "\$lt", toDate
                    )
                )
            }
            if (body.optString("unit_no").isNotEmpty() && !unitNo.isEmpty) {
                default = default + Document(
                    "unit_no", Document("\$in", unitNo)
                )
            }

            if (body.optString("permit_type").isNotEmpty() && !permitType.isEmpty) {
                val permits = fetchPermits(breadcrumb)
                val selected = ArrayList<ObjectId>()
                permitType.forEach { con ->
                    val selectedPermit = permits.find { it["permit"] == con.toString().uppercase(Locale.getDefault()) }
                    val id = selectedPermit?.optString("id")
                    if (!id.isNullOrEmpty()) {
                        selected.add(ObjectId(id))
                    }
                }
                default = default + Document(
                    "permit_type", Document("\$in", selected)
                )
            }

            val filter = Document(
                "\$and", default
            )
            val lookupPipeline = col.aggregate(
                listOf(
                    Document(
                        "\$addFields", Document(
                            "converted", Document(
                                "\$dateToString",
                                Document("date", "\$created_at").append("format", "%Y-%m-%dT%H:%M:%S.%L+08:00")
                                    .append("timezone", "Asia/Manila")
                            )
                        )
                    ), Document(
                        "\$match", filter
                    ), Document(
                        "\$lookup",
                        Document("from", "permit").append("localField", "permit_type").append("foreignField", "_id")
                            .append("as", "permit")
                    ), Document(
                        "\$unwind", Document("path", "\$permit").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$project",
                        Document("permit_id", 1L).append("_id", 0L).append("status", 1L)
                            .append("date_filed", "\$converted").append("type", "\$permit.permit").append("unit_no", 1L)
                    )
                )
            ).toList()
            lookupPipeline.forEach { docs ->
                arrayResponse.add(JSONObject(docs))
            }
            return arrayResponse
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING MODULE")
            e.printStackTrace()
        }
        breadcrumb.log("END FETCHING MODULE")
        return ArrayList<JSONObject>()
    }

    fun fetchPermitApprover(breadcrumb: Breadcrumb, id: String): ArrayList<Document> {
        breadcrumb.log("START FETCHING PERMIT")
        val arrayResponse = ArrayList<Document>()
        try {
            val col = App.mongoDatabase.getCollection("permit")
            val lookupPipeline = col.aggregate(
                listOf(
                    Document(
                        "\$match", Document(
                            "_id", ObjectId(id)
                        )
                    ), Document(
                        "\$unwind", Document("path", "\$approver").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$lookup",
                        Document("from", "position").append("localField", "approver.position_id")
                            .append("foreignField", "_id").append("as", "position_details")
                    ), Document(
                        "\$unwind", Document("path", "\$position_details").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$group", Document("_id", "\$_id").append(
                            "approver", Document(
                                "\$addToSet", Document("rank", "\$approver.rank").append(
                                    "position_id", Document("\$toString", "\$approver.position_id")
                                ).append("position_name", "\$position_details.position_name").append(
                                    "status", Document(
                                        "\$cond", Document(
                                            "if", Document(
                                                "\$eq", listOf("\$position_details.position_name", "ADMIN")
                                            )
                                        ).append("then", "FOR VERIFICATION").append("else", "PENDING")
                                    )
                                ).append("date", "")
                            )
                        )
                    ), Document(
                        "\$addFields", Document(
                            "approver", Document(
                                "\$sortArray", Document("input", "\$approver").append(
                                    "sortBy", Document("rank", 1L)
                                )
                            )
                        )
                    )
                )
            ).toList()
            lookupPipeline.forEach { docs ->
                val docu = JSONObject(docs).optJSONArray("approver")
                docu?.forEach { item ->
                    val jsonCal = JSONObject(item.toString())

                    val document = Document()
                    jsonCal.keySet().forEach { key ->
                        document[key] = identifyKey(jsonCal, key)
                    }
                    arrayResponse.add(document)
                }
            }
            return arrayResponse
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING MODULE")
            e.printStackTrace()
        }
        breadcrumb.log("END FETCHING MODULE")
        return arrayResponse
    }

    fun viewPermitRequestDetails(breadcrumb: Breadcrumb, permitId: String): JSONObject {
        breadcrumb.log("START FETCHING PERMIT DETAILS")
        try {
            val col = App.mongoDatabase.getCollection("permit_request")
            val lookupPipeline = col.aggregate(
                listOf(
                    Document(
                        "\$match", Document("permit_id", permitId)
                    ), Document(
                        "\$lookup",
                        Document("from", "permit").append("localField", "permit_type").append("foreignField", "_id")
                            .append("as", "permit_details")
                    ), Document(
                        "\$unwind", Document("path", "\$permit_details").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$unwind", Document("path", "\$approver").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$addFields", Document(
                            "converted", Document(
                                "\$dateToString",
                                Document("date", "\$created_at").append("format", "%Y-%m-%dT%H:%M:%S.%L+08:00")
                                    .append("timezone", "Asia/Manila")
                            )
                        )
                    ), Document(
                        "\$group", Document("_id", "\$_id").append(
                            "permit_id", Document("\$first", "\$permit_id")
                        ).append(
                            "user_id", Document("\$first", "\$user_id")
                        ).append(
                            "permit_type", Document("\$first", "\$permit_details.permit")
                        ).append(
                            "data", Document("\$first", "\$data")
                        ).append(
                            "remarks", Document("\$first", "\$remarks")
                        ).append(
                            "approver", Document(
                                "\$addToSet",
                                Document("status", "\$approver.status").append("date", "\$approver.date")
                                    .append("rank", "\$approver.rank")
                                    .append("position_name", "\$approver.position_name").append(
                                        "position_id", Document("\$toString", "\$approver.position_id")
                                    )
                            )
                        ).append(
                            "status", Document("\$first", "\$status")
                        ).append(
                            "date_filed", Document("\$first", "\$converted")
                        )
                    ), Document(
                        "\$addFields", Document(
                            "approver", Document(
                                "\$sortArray", Document("input", "\$approver").append(
                                    "sortBy", Document("rank", 1L)
                                )
                            )
                        )
                    )
                )
            ).toList()
            lookupPipeline.forEach { docs ->
                val json = JSONObject()
                docs.keys.filter { it !in listOf("_id") }.sorted().forEach { key ->
                    json.put(key, docs[key])
                }
                json.put("id", docs["_id"].toString())
                json.put("user_id", docs["user_id"].toString())
                return json
            }
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING MODULE")
            e.printStackTrace()
        }
        return JSONObject()
    }

    fun getAllRole(breadcrumb: Breadcrumb): ArrayList<JSONObject> {
        breadcrumb.log("START FETCHING ROLES")
        val arrayResponse = ArrayList<JSONObject>()
        try {
            val col = App.mongoDatabase.getCollection("role")
            val lookupPipeline = col.aggregate(
                listOf(
                    Document(
                        "\$match", Document("status", "ACTIVE")
                    ), Document(
                        "\$unwind", Document("path", "\$modules").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$lookup",
                        Document("from", "module").append("localField", "modules.module_id")
                            .append("foreignField", "_id").append("as", "module_details")
                    ), Document(
                        "\$unwind", Document("path", "\$module_details").append("preserveNullAndEmptyArrays", true)
                    ), Document(
                        "\$lookup",
                        Document("from", "submodule").append("localField", "modules.submodules.module_id")
                            .append("foreignField", "_id").append("as", "submodules_data")
                    ), Document(
                        "\$addFields", Document(
                            "submodules", Document(
                                "\$map", Document(
                                    "input", Document("\$ifNull", listOf("\$modules.submodules", listOf<String>()))
                                ).append("as", "submodule").append(
                                        "in", Document("action", "$\$submodule.action").append(
                                                "submodule_data", Document(
                                                    "\$let", Document(
                                                        "vars", Document(
                                                            "matchedSubmoduleData", Document(
                                                                "\$arrayElemAt", listOf(
                                                                    "\$submodules_data", Document(
                                                                        "\$indexOfArray", listOf(
                                                                            "\$submodules_data._id",
                                                                            "$\$submodule.module_id"
                                                                        )
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    ).append(
                                                            "in", Document(
                                                                "\$mergeObjects", listOf(
                                                                    Document("icon", "$\$matchedSubmoduleData.icon"),
                                                                    Document("name", "$\$matchedSubmoduleData.name"),
                                                                    Document(
                                                                        "module_id", Document(
                                                                            "\$toString", "$\$matchedSubmoduleData._id"
                                                                        )
                                                                    )
                                                                )
                                                            )
                                                        )
                                                )
                                            )
                                    )
                            )
                        )
                    ), Document(
                        "\$group", Document("_id", "\$_id").append(
                                "role", Document("\$first", "\$role")
                            ).append(
                                "modules", Document(
                                    "\$addToSet", Document(
                                        "\$mergeObjects", Document(
                                            "module_id", Document("\$toString", "\$modules.module_id")
                                        ).append("name", "\$module_details.name").append("action", "\$modules.action")
                                            .append(
                                                "submodules", Document(
                                                    "\$sortArray", Document("input", "\$submodules").append(
                                                            "sortBy", Document("submodule_data.position", 1L)
                                                        )
                                                )
                                            )
                                    )
                                )
                            )
                    )
                )
            ).toList()
            lookupPipeline.forEach { docs ->
                val json = JSONObject()
                docs.keys.filter { it !in listOf("_id") }.sorted().forEach { key ->
                    json.put(key, docs[key])
                }

                json.put("id", docs["_id"].toString())
                arrayResponse.add(json)
            }
            return arrayResponse
        } catch (e: Throwable) {
            breadcrumb.log("ERROR ON FETCHING ROLES")
            e.printStackTrace()
        }
        return arrayResponse
    }


    private fun identifyKey(body: JSONObject, params: String): Any {
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

    private fun convertDateToISO(date: String): String {
        val formattedDateString = date.replace(" ", "T")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val localDateTime = LocalDateTime.parse(formattedDateString, formatter)
        val instant = localDateTime.atOffset(ZoneOffset.UTC).toInstant()
        println(instant)
        return "${instant}+08:00"
    }
}