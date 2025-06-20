package dev.windmill_broken.cobblemon_store.dao

import dev.windmill_broken.cobblemon_store.bo.trade.Cost
import dev.windmill_broken.cobblemon_store.bo.trade.Purchasing
import dev.windmill_broken.cobblemon_store.bo.trade.ServerTradeCreator
import dev.windmill_broken.cobblemon_store.bo.trade.StoreLimit
import dev.windmill_broken.cobblemon_store.bo.trade.Trade
import dev.windmill_broken.cobblemon_store.bo.trade.TradeCreator

interface TradeLibrary : DAO {
    fun get(tradeId : Int) : Trade?

    fun createTrade(
        storeId : String,
        creator: TradeCreator = ServerTradeCreator,
        cost : Cost,
        purchasing : Purchasing,
        storeLimits : Set<StoreLimit> = emptySet()
    )

    fun getByStoreId(storeId : String) : Collection<Trade>

    fun update(trade : Trade)

    fun removeById(id : Int)

    fun register(){}
}