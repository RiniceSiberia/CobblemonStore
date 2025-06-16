package com.xxxt.cobblemon_store.store

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.function.IntFunction

class Store(
    val id : String,
    var name : String,
    val description: String? = null,
    val trades : MutableList<Trade> = mutableListOf()
) : MutableList<Trade> by trades{


    override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?>? {
        return super.toArray(generator)
    }

    fun serialize() : JsonObject {
        val obj = JsonObject()
        obj.addProperty("id", id)
        obj.addProperty("name", name)
        obj.addProperty("description", description)
        val array = JsonArray()
        trades.forEach { array.add(it.serialize()) }
        obj.add("trades", array)
        return obj
    }

    companion object{
        fun deserialize(json: JsonObject) : Store{
            val trades = mutableListOf<Trade>()
            json.get("trades").asJsonArray.forEach {
                Trade.deserialize(it.asJsonObject)?.let {
                    element -> trades.add(element)
                }
            }
            return Store(
                json.get("id").asString,
                json.get("name").asString,
                json.get("description").asString,
                trades
            )
        }
    }
}