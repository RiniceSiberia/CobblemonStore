package dev.windmill_broken.cobblemon_store.bo.store

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.windmill_broken.cobblemon_store.bo.trade.Trade
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import kotlinx.serialization.Serializable
import java.util.function.IntFunction

@Serializable
class Store(
    val id : String,
    var name : String,
    val description: String? = null,
){
    val trades : Collection<Trade>
        get() = DAOWharf.tradeLibrary.getByStoreId(id)

}