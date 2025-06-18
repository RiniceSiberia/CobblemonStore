package dev.windmill_broken.cobblemon_store.dao

import dev.windmill_broken.cobblemon_store.bo.store.Store
import dev.windmill_broken.cobblemon_store.bo.trade.CostObj
import dev.windmill_broken.cobblemon_store.bo.trade.PurchasingObj
import dev.windmill_broken.cobblemon_store.bo.trade.StoreLimit
import dev.windmill_broken.money_lib.dao.DAO

interface StoresLibrary : DAO {

    operator fun get(sid : String) : Store?

    fun create(
        id : String,
        name : String,
        description: String? = null,
        tradeValues: List<Triple<CostObj, PurchasingObj, Map<String, StoreLimit>>>
        )

    fun update(id : String, store : Store)

    fun removeById(id : String)

    fun register(){}
}