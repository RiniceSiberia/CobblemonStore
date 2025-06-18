package dev.windmill_broken.cobblemon_store.dao.database.meta

import dev.windmill_broken.cobblemon_store.bo.warehouse.Warehouse
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.kJsonConfig
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.json.jsonb

object WarehouseLibraryDBTable : UUIDTable("warehouse","p_id") {
    val warehouse = jsonb("warehouse", jsonConfig = kJsonConfig, Warehouse.serializer())
}