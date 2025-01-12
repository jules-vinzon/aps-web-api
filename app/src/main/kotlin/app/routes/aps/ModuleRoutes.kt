package app.routes.aps

import app.App
import app.services.aps.module.*
import app.utils.Breadcrumb
import io.javalin.apibuilder.ApiBuilder

class ModuleRoutes {
    val add = AddModule()
    val get = GetModule()
    fun start() {
        App.javalin.routes {
            ApiBuilder.path("/module") {
                ApiBuilder.post("/add") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /module/add Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = add.addModule(breadcrumb, it)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /module/add Route")
                }

                ApiBuilder.get("/get") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /module/get Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val requestId = it.queryParam("request_id").toString()
                    val processService = get.getModule(breadcrumb, requestId)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /module/get Route")
                }

                ApiBuilder.get("/get/all") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /module/get/all Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val requestId = it.queryParam("request_id").toString()
                    val processService = get.getAllModule(breadcrumb, requestId)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /module/get/all Route")
                }
            }
        }
    }
}