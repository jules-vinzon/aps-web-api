package app.routes.aps

import app.App
import app.services.aps.employee.AddPosition
import app.services.aps.employee.GetPosition
import app.services.aps.employee.Register
import app.utils.Breadcrumb
import io.javalin.apibuilder.ApiBuilder

class EmployeeRoutes {
    val add = AddPosition()
    val get = GetPosition()
    val reg = Register()

    fun start() {
        App.javalin.routes {
            ApiBuilder.path("/employee") {
                ApiBuilder.post("/register") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /employee/register Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = reg.register(breadcrumb, it)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /employee/register Route")
                }

                ApiBuilder.post("/add/position") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /add/position Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = add.addPosition(breadcrumb, it)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /add/position Route")
                }

                ApiBuilder.get("/get/position") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /get/position Route")
                    val requestId = it.queryParam("request_id").toString()

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = get.getPosition(breadcrumb, requestId)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /get/position Route")
                }
            }
        }
    }
}