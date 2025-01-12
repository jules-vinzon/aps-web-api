package app.routes.parcel

import app.App
import app.services.parcel_logger.parcel.AddParcelServices
import app.services.parcel_logger.sms.CallbackService
import app.utils.Breadcrumb
import io.javalin.apibuilder.ApiBuilder

class SmsRoutes {
    val callback = CallbackService()
    fun start() {
        App.javalin.routes {
            ApiBuilder.post("/sms/callback") {
                val breadcrumb = Breadcrumb()
                val breadcrumbId = breadcrumb.log("START: /sms/callback Route")

                it.header("Content-Type", "application/json; charset=UTF-8")
                it.header("Accept", "application/json")
                it.header("Breadcrumb", breadcrumbId)

                val processService = callback.callback(breadcrumb, it)

                it.result(processService.optString("response"))
                it.status(processService.optInt("HttpStatus", 200))

                breadcrumb.log("FINAL RESPONSE: $processService")
                breadcrumb.log("END: /add/parcel Route")
            }

            ApiBuilder.post("/test") {
                it.result(AddParcelServices().parseDate(it.body()))
            }
        }
    }
}