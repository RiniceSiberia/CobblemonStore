package dev.windmill_broken.cobblemon_store.dao

import dev.windmill_broken.cobblemon_store.bo.trade.CostObj
import dev.windmill_broken.cobblemon_store.bo.trade.PurchasingObj
import dev.windmill_broken.cobblemon_store.bo.trade.StoreLimit
import dev.windmill_broken.cobblemon_store.bo.trade.Trade

interface TradeLibrary : DAO {
    fun get(tradeId : Int) : Trade?

    fun createTrade(
        storeId : String,
        cost : CostObj,
        purchasing : PurchasingObj,
        limit : Map<String, StoreLimit> = emptyMap()
    )

    fun getByStoreId(storeId : String) : Collection<Trade>

    fun update(trade : Trade)

    fun removeById(id : Int)

    fun register(){}
}