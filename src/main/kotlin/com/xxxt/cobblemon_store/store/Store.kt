package com.xxxt.cobblemon_store.store

import com.google.gson.JsonArray
import com.google.gson.JsonObject

class Store(
    val id : Int,
    var name : String = "Store $id",
    val description: String?,
    val trades : MutableList<Trade> = mutableListOf()
) : MutableList<Trade> by trades{


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
                json.get("id").asInt,
                json.get("name").asString,
                json.get("description").asString,
                trades
            )
        }
    }
}