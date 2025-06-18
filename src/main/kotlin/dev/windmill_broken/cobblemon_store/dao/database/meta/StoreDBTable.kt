package dev.windmill_broken.cobblemon_store.dao.database.meta

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object StoreDBTable : IdTable<String>("store") {
    override val id: Column<EntityID<String>>
    = text("s_id").entityId()
    val name = text("name")
    val description = text("description").nullable()
}