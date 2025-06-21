package dev.windmill_broken.cobblemon_store.dao.database

import dev.windmill_broken.cobblemon_store.bo.warehouse.Warehouse
import dev.windmill_broken.cobblemon_store.dao.DAO
import dev.windmill_broken.cobblemon_store.dao.WarehouseLibrary
import dev.windmill_broken.cobblemon_store.utils.DatabaseUtils
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.kJsonConfig
import java.util.*

object WarehouseDBLibrary : WarehouseLibrary, DAO.DBDAO{
    init {
        register()
    }

    override val tableName: String
        get() = "cobblemon_store_warehouses"

    override fun createTable() {
        DatabaseUtils.getConnection().use {
            it.prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS $tableName (
                    p_id CHAR(36) PRIMARY KEY,
                    warehouse JSON NOT NULL
                )
                """.trimIndent()
            ).execute()
        }
    }

    override fun getOrCreate(pid: UUID): Warehouse {
        DatabaseUtils.getConnection().use { conn ->
            conn.prepareStatement("SELECT $tableName FROM $tableName WHERE p_id = ?").use { stmt ->
                stmt.setString(1, pid.toString())
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        val jsonStr = rs.getString("warehouse")
                        return kJsonConfig.decodeFromString(jsonStr)
                    }
                }
            }

            // 如果不存在就创建一个空仓库
            val newWarehouse = Warehouse(pid)
            update(pid, newWarehouse)
            return newWarehouse
        }
    }

    override fun update(pid: UUID, warehouse: Warehouse) {
        DatabaseUtils.getConnection().use {
            it.prepareStatement(
                """
                INSERT INTO $tableName (p_id, warehouse)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE warehouse = VALUES(warehouse)
                """.trimIndent()
            ).apply {
                setString(1, pid.toString())
                setString(2, kJsonConfig.encodeToString(warehouse))
                executeUpdate()
            }
        }
    }

    override fun register() {
        if (valid && !exists()){
            createTable()
        }
    }
}