package dev.windmill_broken.cobblemon_store.dao

import dev.windmill_broken.cobblemon_store.bo.store.Store
import dev.windmill_broken.cobblemon_store.bo.trade.Cost
import dev.windmill_broken.cobblemon_store.bo.trade.Purchasing
import dev.windmill_broken.cobblemon_store.bo.trade.StoreLimit

interface StoresLibrary : DAO {

    operator fun get(sid : String) : Store?

    fun create(
        id : String,
        name : String,
        description: String? = null,
        tradeValues: List<Triple<Cost, Purchasing, Set<StoreLimit>>> = emptyList()
        )

    fun update(id : String, store : Store)

    fun removeById(id : String)

    fun register(){}
}