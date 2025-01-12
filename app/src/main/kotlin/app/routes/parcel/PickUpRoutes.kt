package app.routes.parcel

import app.App
import app.services.parcel_logger.parcel.FetchParcelServices
import app.services.parcel_logger.pickUp.AddPickUp
import app.services.parcel_logger.pickUp.FetchPickUp
import app.services.parcel_logger.pickUp.UpdatePickUp
import app.utils.Breadcrumb
import io.javalin.apibuilder.ApiBuilder

class PickUpRoutes {
    fun start() {
        val add = AddPickUp()
        val fetch = FetchPickUp()
        val update = UpdatePickUp()
        val parcelLogger = FetchParcelServices()

        App.javalin.routes {
            ApiBuilder.path("/pick-up") {

                ApiBuilder.post("/parcel/add") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /add/parcel Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = add.pickUpParcel(breadcrumb, it)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("Pick up")
                    breadcrumb.log("response: ${it.body()}")

                }

                ApiBuilder.get("/fetch/{id}") {
                    val breadcrumb = Breadcrumb()
                    val pickupId = it.pathParam("id")
                    val breadcrumbId = breadcrumb.log("START: /fetch/${pickupId} Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = fetch.fetchPickUp(breadcrumb, pickupId)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("Fetch Pick Up")
                    breadcrumb.log("response: ${it.body()}")


                }

                ApiBuilder.get("/fetch/status/{status}") {
                    val breadcrumb = Breadcrumb()
                    val status = it.pathParam("status")
                    val breadcrumbId = breadcrumb.log("START: /fetch/status/${status} Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = fetch.fetchStatus(breadcrumb, status)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("Fetch Pick Up")
                    breadcrumb.log("response: ${it.body()}")
                }

                ApiBuilder.get("/dates/fetch") {
                    val breadcrumb = Breadcrumb()
                    val from = it.queryParam("from").toString()
                    val to = it.queryParam("to").toString()
                    val breadcrumbId = breadcrumb.log("START: /fetch/dates?from=${from}&to=${to} Route")
                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService =  fetch.fetchDates(breadcrumb, from, to)

                    it.result(processService.optString("response"))
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /fetch/all/parcel Route")
                }

                ApiBuilder.post("/parcel/update") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /pickUp/parcel Route")

                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = update.pickUpUpdate(breadcrumb, it)

                    it.result(processService.toString())
                    it.status(processService.optInt("HttpStatus", 200))

                    breadcrumb.log("Update Pick Up")
                    breadcrumb.log("response: ${it.body()}")

                }

                ApiBuilder.get("/fetch/95days/pickup") {
                    val breadcrumb = Breadcrumb()
                    val breadcrumbId = breadcrumb.log("START: /fetch/95days/pickup Route")
                    it.header("Content-Type", "application/json; charset=UTF-8")
                    it.header("Breadcrumb", breadcrumbId)

                    val processService = parcelLogger.updateListParcel(breadcrumb)

                    it.result(App.allPickUp.toString())
                    it.status(200)

                    breadcrumb.log("FINAL RESPONSE: $processService")
                    breadcrumb.log("END: /fetch/95days/pickup Route")
                }
            }
        }
    }
}
