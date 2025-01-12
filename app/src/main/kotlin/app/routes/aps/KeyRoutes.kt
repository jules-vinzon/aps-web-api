package app.routes.aps

import app.App
import app.services.aps.key.GetKey
import app.utils.Breadcrumb
import io.javalin.apibuilder.ApiBuilder

class KeyRoutes {
    val get = GetKey()
    fun start() {
        App.javalin.routes {
            ApiBuilder.post("/get/key") {
                val breadcrumb = Breadcrumb()
                val breadcrumbId = breadcrumb.log("START: /get/key Route")

                it.header("Content-Type", "application/json; charset=UTF-8")
                it.header("Accept", "application/json")
                it.header("Breadcrumb", breadcrumbId)

                val processService = get.getKey(breadcrumb, it)

                it.result(processService.optString("response"))
                it.status(processService.optInt("HttpStatus", 200))

                breadcrumb.log("FINAL RESPONSE: $processService")
                breadcrumb.log("END: /get/key Route")
            }
        }
    }
}