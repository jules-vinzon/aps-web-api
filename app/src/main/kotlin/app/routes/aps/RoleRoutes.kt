package app.routes.aps

import app.App
import app.services.aps.role.AddRole
import app.services.aps.role.FetchUserRole
import app.services.aps.role.GetRole
import app.utils.Breadcrumb
import io.javalin.apibuilder.ApiBuilder

class RoleRoutes {
    val add = AddRole()
    val get = GetRole()
    val getUserRole = FetchUserRole()
    fun start() {
        App.javalin.routes {
            ApiBuilder.path("/role") {
                ApiBuilder.post("/add") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /role/add Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = add.addRole(breadcrumb, it)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /role/add Route")
                }

                ApiBuilder.get("/get") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /role/get Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val requestId = it.queryParam("request_id").toString()
                    val processService = get.getRole(breadcrumb, requestId)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /role/get Route")
                }

                ApiBuilder.get("/get/user/role") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /role/get/resident/role Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = getUserRole.fetchTenantRole(breadcrumb, it)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /role/get/resident/role Route")
                }
            }
        }
    }
}