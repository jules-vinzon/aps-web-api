package app.database.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime

object Merchant : Table() {
    val tenant_id = uuid("tenant_id")
    val prefix = varchar("prefix", 255)
    val external_id = varchar("external_id", 255)

    val merchant_id = varchar("merchant_id", 255)
    val legal_name = varchar("legal_name", 255)
    val name = varchar("name", 255)

    val status = uuid("status")
    val mkey = varchar("mkey", 255)
    val created_at = datetime("created_at")
    val updated_at = datetime("updated_at")
}
