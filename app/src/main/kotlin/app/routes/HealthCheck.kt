package app.routes

import app.App
import io.javalin.apibuilder.ApiBuilder

class HealthCheck {
    fun start() {
        App.javalin.routes {
            ApiBuilder.get("/healthcheck") {
                it.result("OK")
            }
        }
    }
}