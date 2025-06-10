package com.xxxt.cobblemon_store.database.dao

import com.xxxt.cobblemon_store.database.table.StoresTable
import com.xxxt.cobblemon_store.database.table.TradesTable
import com.xxxt.cobblemon_store.store.Store
import com.xxxt.cobblemon_store.utils.DataBaseUtils
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class StoreEntity(id : EntityID<Int>) : IntEntity(id){
    companion object : EntityClass<Int, StoreEntity>(table = StoresTable)

    var name by StoresTable.name
    var description by StoresTable.description

    fun toBO() : Store{
        return Store(
            id = id.value,
            name = name,
            description = description,
            trades = transaction(DataBaseUtils.db.value) {
                TradeEntity.find {
                    TradesTable.store eq this@StoreEntity.id
                }.map {
                    it.content
                }.toMutableList()
            }
        )
    }
}