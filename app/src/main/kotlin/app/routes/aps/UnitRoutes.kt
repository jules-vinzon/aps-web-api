package app.routes.aps

import app.App
import app.services.aps.unit.AddUnit
import app.services.aps.unit.FetchUnit
import app.utils.Breadcrumb
import io.javalin.apibuilder.ApiBuilder

class UnitRoutes {
    private val add = AddUnit()
    private val get = FetchUnit()
    fun start() {
        App.javalin.routes {
            ApiBuilder.path("/unit") {
                ApiBuilder.post("/add") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /unit/add Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = add.addUnit(breadcrumb, it)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /unit/add Route")
                }

                ApiBuilder.get("/get") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /unit/get Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = get.getUnit(breadcrumb, it)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /unit/get Route")
                }
            }
        }
    }
}