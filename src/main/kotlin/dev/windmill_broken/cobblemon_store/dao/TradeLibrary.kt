package dev.windmill_broken.cobblemon_store.dao

import dev.windmill_broken.cobblemon_store.bo.trade.*

interface TradeLibrary : DAO {
    fun get(tradeId : Int) : Trade?

    fun create(
        storeId : String,
        creator: TradeCreator = ServerTradeCreator,
        autoRemove: Boolean = false,
        cost : Cost,
        purchasing : Purchasing,
        storeLimits : Set<StoreLimit> = emptySet()
    ): Int

    fun getByStoreId(storeId : String) : Collection<Trade>

    fun export() : List<Trade>

    fun import(trades : Collection<Trade>)

    fun update(
        trade: Trade
    )

    fun update(
        id: Int,
        storeId : String,
        creator: TradeCreator,
        autoRemove: Boolean,
        cost: Cost,
        purchasing: Purchasing,
        storeLimits: Set<StoreLimit>
    )

    fun removeById(id : Int)
}