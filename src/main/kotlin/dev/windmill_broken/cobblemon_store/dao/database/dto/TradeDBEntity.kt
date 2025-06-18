package dev.windmill_broken.cobblemon_store.dao.database.dto

import dev.windmill_broken.cobblemon_store.dao.database.meta.TradeDBTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TradeDBEntity(id : EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, TradeDBEntity>(TradeDBTable)

    var store by StoreDBEntity referencedOn TradeDBTable.storeId
    var cost by TradeDBTable.cost
    var purchasing by TradeDBTable.purchasing
    var storeLimits by TradeDBTable.storeLimits
}