package dev.windmill_broken.cobblemon_store.bo.store

import dev.windmill_broken.cobblemon_store.bo.trade.Trade
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import kotlinx.serialization.Serializable

@Serializable
class Store(
    val id : String,
    var name : String,
    val description: String? = null,
){
    val trades : Collection<Trade>
        get() = DAOWharf.tradeLibrary.getByStoreId(id)

}