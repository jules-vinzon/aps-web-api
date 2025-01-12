package app.routes.aps

import app.App
import io.javalin.apibuilder.ApiBuilder

class MainRoutes {
    fun start() {
        App.javalin.routes {
            ApiBuilder.path("/aps") {
                EmployeeRoutes().start()
                ModuleRoutes().start()
                SubModuleRoutes().start()
                UserRoutes().start()
                KeyRoutes().start()
                TenantRoutes().start()
                RoleRoutes().start()
                PermitRoutes().start()
                UnitRoutes().start()
                NotificationRoutes().start()
            }
        }
    }
}