package com.xxxt.cobblemon_store.store

import com.xxxt.cobblemon_store.database.dao.StoreEntity
import com.xxxt.cobblemon_store.database.table.StoresTable
import com.xxxt.cobblemon_store.database.table.TradesTable
import com.xxxt.cobblemon_store.utils.DataBaseUtils
import com.xxxt.cobblemon_store.utils.jsonConfig
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.json.json
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

object StoresLibrary : MutableMap<Int, Store> {

    val stores = mutableMapOf<Int, Store>()

    override val size: Int
        get() = stores.size

    override val keys: MutableSet<Int>
        get() = stores.keys
    override val values: MutableCollection<Store>
        get() = stores.values
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Store>>
        get() = stores.entries

    override fun isEmpty(): Boolean = stores.isEmpty()

    override fun containsKey(key: Int): Boolean = stores.containsKey(key)

    override fun containsValue(value: Store): Boolean = stores.containsValue(value)

    override fun get(key: Int): Store? = stores[key]

    override fun put(
        key: Int,
        value: Store
    ): Store? = stores.put(key, value)

    override fun remove(key: Int): Store? = stores.remove(key)

    override fun putAll(from: Map<out Int, Store>) = stores.putAll(from)

    override fun clear() = stores.clear()

    fun register(){
        DataBaseUtils.register()
        transaction(DataBaseUtils.db.value) {
            StoreEntity.all().map {
                it.id.value to it.toBO()
            }
        }.also {
            this.putAll(it)
        }
    }

    fun save(){
        transaction(DataBaseUtils.db.value) {
            StoresTable.batchUpsert(
                data = stores.values,
                onUpdateExclude = listOf(StoresTable.id)
            ) { store ->
                this[StoresTable.name] = store.name
                this[StoresTable.description] = store.description
            }

            TradesTable.batchUpsert(
                data = stores.values.map {
                    it.id to it.trades
                },
                onUpdateExclude = listOf(TradesTable.id)
            ){ (storeId,trades) ->
                trades.forEach { t ->
                    this[TradesTable.store] = storeId
                    this[TradesTable.content] = t
                }
            }
        }
    }
}