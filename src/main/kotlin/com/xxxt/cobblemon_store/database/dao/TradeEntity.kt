package com.xxxt.cobblemon_store.database.dao

import com.xxxt.cobblemon_store.database.table.TradesTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TradeEntity(id : EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TradeEntity>(TradesTable)

    var content by TradesTable.content
    var store by StoreEntity referencedOn TradesTable.store

}