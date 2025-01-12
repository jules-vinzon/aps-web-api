package app.routes.aps

import app.App
import app.utils.Breadcrumb
import app.utils.Mqtt
import io.javalin.apibuilder.ApiBuilder
import org.json.JSONObject

class NotificationRoutes {
    val mqtt = Mqtt()
    fun start() {
        App.javalin.routes {
            ApiBuilder.path("/notification") {
                ApiBuilder.post("/publish") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /notification/publish Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Accept", "application/json")
                    it.header("Breadcrumb", breadcrumbId)

                    val body = JSONObject(it.body())
                    val topic = body.optString("topic")
                    val msg = body.optString("message")

                    mqtt.subscribe(breadcrumb, topic)
                    mqtt.publish(breadcrumb, topic, msg)

                    it.result("OK")
                    it.status(200)

                    breadcrumb.log("END: /notification/publish Route")
                }
            }
        }
    }
}