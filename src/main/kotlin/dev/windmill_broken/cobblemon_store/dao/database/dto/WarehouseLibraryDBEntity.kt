package dev.windmill_broken.cobblemon_store.dao.database.dto

import dev.windmill_broken.cobblemon_store.dao.database.meta.WarehouseLibraryDBTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class WarehouseLibraryDBEntity(
    id : EntityID<UUID>
) : UUIDEntity(id){
    companion object : UUIDEntityClass<WarehouseLibraryDBEntity>(WarehouseLibraryDBTable) {}

    var warehouse by WarehouseLibraryDBTable.warehouse
}