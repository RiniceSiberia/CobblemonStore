package dev.windmill_broken.cobblemon_store.dao.database.dto

import dev.windmill_broken.cobblemon_store.dao.database.meta.StoreDBTable
import dev.windmill_broken.cobblemon_store.dao.database.meta.TradeDBTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class StoreDBEntity(
    id : EntityID<String>,
) : Entity<String>(id) {
    companion object : EntityClass<String, StoreDBEntity>(StoreDBTable)

    var name by StoreDBTable.name

    var description by StoreDBTable.description

    val trades by TradeDBEntity referrersOn TradeDBTable.storeId
}