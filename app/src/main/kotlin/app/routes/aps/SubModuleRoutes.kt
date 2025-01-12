package app.routes.aps

import app.App
import app.services.aps.module.AddSubModule
import app.services.aps.module.GetModule
import app.utils.Breadcrumb
import io.javalin.apibuilder.ApiBuilder

class SubModuleRoutes {
    val add = AddSubModule()
    val get = GetModule()
    fun start() {
        App.javalin.routes {
            ApiBuilder.path("/submodule") {
                ApiBuilder.post("/add") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /add/submodule Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = add.addSubModule(breadcrumb, it)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /add/submodule Route")
                }

                ApiBuilder.get("/get") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /submodule/get Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val requestId = it.queryParam("request_id").toString()
                    val processService = get.getSubModule(breadcrumb, requestId)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /submodule/get Route")
                }
            }
        }
    }
}