package app.routes.aps

import app.App
import app.services.aps.user.Logout
import app.services.aps.user.Login
import app.utils.Breadcrumb
import io.javalin.apibuilder.ApiBuilder

class UserRoutes {
    private val login = Login()
    private val logout = Logout()
    fun start() {
        App.javalin.routes {
            ApiBuilder.post("/login") {
                val breadcrumb = Breadcrumb()
                val breadcrumbId = breadcrumb.log("START: /login/tenant Route")

                it.header("Content-Type", "application/json; charset=UTF-8")
                it.header("Accept", "application/json")
                it.header("Breadcrumb", breadcrumbId)

                val processService = login.login(breadcrumb, it)

                it.result(processService.optString("response"))
                it.status(processService.optInt("HttpStatus", 200))

                breadcrumb.log("FINAL RESPONSE: $processService")
                breadcrumb.log("END: /login/tenant Route")
            }

            ApiBuilder.post("/validate/token") {
                val breadcrumb = Breadcrumb()
                val breadcrumbId = breadcrumb.log("START: /validate/token Route")

                it.header("Content-Type", "application/json; charset=UTF-8")
                it.header("Accept", "application/json")
                it.header("Breadcrumb", breadcrumbId)

                val processService = login.validateToken(breadcrumb, it)

                it.result(processService.optString("response"))
                it.status(processService.optInt("HttpStatus", 200))

                breadcrumb.log("FINAL RESPONSE: $processService")
                breadcrumb.log("END: /validate/token Route")
            }


            ApiBuilder.post("/logout") {
                val breadcrumb = Breadcrumb()
                val breadcrumbId = breadcrumb.log("START: /logout Route")

                it.header("Content-Type", "application/json; charset=UTF-8")
                it.header("Accept", "application/json")
                it.header("Breadcrumb", breadcrumbId)

                val processService = logout.logout(breadcrumb, it)

                it.result(processService.optString("response"))
                it.status(processService.optInt("HttpStatus", 200))

                breadcrumb.log("FINAL RESPONSE: $processService")
                breadcrumb.log("END: /logout Route")
            }
        }
    }
}