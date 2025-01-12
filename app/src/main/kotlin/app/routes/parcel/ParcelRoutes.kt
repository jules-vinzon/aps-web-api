package app.routes.parcel

import app.services.parcel_logger.parcel.AddParcelServices
import app.App
import app.services.parcel_logger.parcel.FetchParcelServices
import app.services.parcel_logger.parcel.UpdateParcelServices
import app.utils.Breadcrumb
import io.javalin.apibuilder.ApiBuilder

class ParcelRoutes {
    fun start() {
        val add = AddParcelServices()
        val fetch = FetchParcelServices()
        val update = UpdateParcelServices()

        App.javalin.routes {
            ApiBuilder.post("/add/bulk/parcel") {
                val breadcrumb = Breadcrumb()
                val breadcrumbId = breadcrumb.log("START: /add/bulk/parcel Route")

                it.header("Content-Type", "application/json; charset=UTF-8")
                it.header("Accept", "application/json")
                it.header("Breadcrumb", breadcrumbId)

                val processService = add.addBulkParcel(breadcrumb, it)

                it.result(processService.optString("response"))
                it.status(processService.optInt("HttpStatus", 200))

                breadcrumb.log("FINAL RESPONSE: $processService")
                breadcrumb.log("END: /add/parcel Route")
            }

            ApiBuilder.get("/fetch/parcel/{id}") {
                val breadcrumb = Breadcrumb()
                val breadcrumbId = breadcrumb.log("START: /fetch/parcel Route")

                it.header("Content-Type", "application/json; charset=UTF-8")
                it.header("Breadcrumb", breadcrumbId)

                val processService = fetch.fetchParcel(breadcrumb, it.pathParam("id"))

                it.result(processService.optString("response"))
                it.status(processService.optInt("HttpStatus", 200))

                breadcrumb.log("FINAL RESPONSE: $processService")
                breadcrumb.log("END: /fetch/parcel Route")
            }

            ApiBuilder.get("/fetch/all/parcel") {
                val breadcrumb = Breadcrumb()
                val breadcrumbId = breadcrumb.log("START: /fetch/all/parcel Route")
                val from = it.queryParam("from").toString()
                val to = it.queryParam("to").toString()
                it.header("Content-Type", "application/json; charset=UTF-8")
                it.header("Breadcrumb", breadcrumbId)

                val processService =  fetch.fetchAllParcel(breadcrumb, from, to)

                it.result(processService.optString("response"))
                it.status(processService.optInt("HttpStatus", 200))

                breadcrumb.log("FINAL RESPONSE: $processService")
                breadcrumb.log("END: /fetch/all/parcel Route")
            }

            ApiBuilder.get("/fetch/95days/parcel") {
                val breadcrumb = Breadcrumb()
                val breadcrumbId = breadcrumb.log("START: /fetch/95days/parcel Route")
                it.header("Content-Type", "application/json; charset=UTF-8")
                it.header("Breadcrumb", breadcrumbId)

                val processService = fetch.updateListParcel(breadcrumb)

                it.result(App.allParcel.toString())
                it.status(200)

                breadcrumb.log("FINAL RESPONSE: $processService")
                breadcrumb.log("END: /fetch/95days/parcel Route")
            }

            ApiBuilder.get("/fetch/all/parcel/{parcelID}") {
                val breadcrumb = Breadcrumb()
                val parcelID = it.pathParam("parcelID")
                val breadcrumbId = breadcrumb.log("START: /fetch/all/parcel/$parcelID Route")

                it.header("Content-Type", "application/json; charset=UTF-8")
                it.header("Breadcrumb", breadcrumbId)

                val processService =  fetch.fetchByStatus(breadcrumb, parcelID)

                it.result(processService.optString("response"))
                it.status(processService.optInt("HttpStatus", 200))

                breadcrumb.log("FINAL RESPONSE: $processService")
                breadcrumb.log("END: /fetch/all/parcel Route")
            }

            ApiBuilder.get("/fetch/all/by/rider/{qr}") {
                val breadcrumb = Breadcrumb()
                val qr = it.pathParam("qr")
                val breadcrumbId = breadcrumb.log("START: /fetch/all/by/rider/$qr Route")
                it.header("Content-Type", "application/json; charset=UTF-8")
                it.header("Breadcrumb", breadcrumbId)

                val processService =  fetch.fetchByRiderQr(breadcrumb, qr)

                it.result(processService.optString("response"))
                it.status(processService.optInt("HttpStatus", 200))

                breadcrumb.log("FINAL RESPONSE: $processService")
                breadcrumb.log("END: /fetch/all/by/rider/$qr Route")
            }

            ApiBuilder.post("/update/parcel") {
                val breadcrumb = Breadcrumb()
                val breadcrumbId = breadcrumb.log("START: /update/parcel Route")

                it.header("Content-Type", "application/json; charset=UTF-8")
                it.header("Breadcrumb", breadcrumbId)

                val processService = update.updateParcel(breadcrumb, it)

                it.result(processService.optString("response"))
                it.status(processService.optInt("HttpStatus", 200))

                breadcrumb.log("FINAL RESPONSE: $processService")
                breadcrumb.log("END: /update/parcel Route")
            }

            ApiBuilder.post("/bulk/update/parcel") {
                val breadcrumb = Breadcrumb()
                val breadcrumbId = breadcrumb.log("START: /update/parcel Route")

                it.header("Content-Type", "application/json; charset=UTF-8")
                it.header("Breadcrumb", breadcrumbId)

                val processService = update.updateBulkParcel(breadcrumb, it)

                it.result(processService.optString("response"))
                it.status(processService.optInt("HttpStatus", 200))

                breadcrumb.log("FINAL RESPONSE: $processService")
                breadcrumb.log("END: /bulk/update/parcel Route")
            }
        }
    }
}