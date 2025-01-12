package app.routes.aps

import app.App
import app.services.aps.tenant.RegisterTenant
import app.utils.Breadcrumb
import io.javalin.apibuilder.ApiBuilder

class TenantRoutes {
    private val reg = RegisterTenant()
    fun start() {
        App.javalin.routes {
            ApiBuilder.path("/tenant") {
                ApiBuilder.post("/register") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /register Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = reg.register(breadcrumb, it)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /register Route")
                }
            }
        }
    }
}