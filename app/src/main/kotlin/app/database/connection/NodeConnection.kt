package app.database.connection

import app.App.Companion.dotenv
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource


object NodeConnection {
    lateinit var nodeDataSource: HikariDataSource

    fun init() {
        val config = HikariConfig()
        config.jdbcUrl =
            "jdbc:pgsql://${dotenv["NODE_DB_HOST"].toString()}:${dotenv["NODE_DB_PORT"].toString()}/${dotenv["NODE_DB_NAME"].toString()}"
        config.connectionTimeout = 30_000
        config.maxLifetime = 1_800_000
        config.idleTimeout = 600_000
        config.username = dotenv["NODE_DB_USERNAME"].toString()
        config.password = dotenv["NODE_DB_PASSWORD"].toString()
        nodeDataSource = HikariDataSource(config)
    }
}
