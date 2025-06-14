package com.xxxt.cobblemon_store.store

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player
import java.util.UUID


/**
 * 实现该接口为有不同程度上的存货上限
 */
sealed interface Reserve{
    fun couldBuy(player: Player) : Boolean

    fun consume(player: Player)

    fun restock()

    fun getTooltipComponent(player: Player) : MutableComponent

    fun serialize() : JsonElement

    companion object{
        fun deserialize(json : JsonObject) : Reserve?{
            if (json.has("type") && json.get("type").isJsonPrimitive){
                val type = json.get("type").asString
                when (type) {
                    "global_reserve" -> return GlobalReserve.deserialize(json)
                    "simple_single_reserve" -> return SimpleSingleReserve.deserialize(json)
                }
            }
            return null
        }
    }
}

class GlobalReserve(
    val limit : Int,
    var sales : Int = 0,
//    val identity : Set<StoreIdentity> = emptySet()
) : Reserve{
    override fun couldBuy(player: Player): Boolean {
        return sales < limit
    }

    override fun consume(player: Player) {
        sales++
    }

    override fun restock() {
        sales = 0
    }

    override fun getTooltipComponent(player: Player): MutableComponent {
        return Component.translatable(
            "item.cobblemon_store.sell_menu.slot.reserve.global",
            limit-sales,
            limit
            ).apply {
                if (sales >= limit){
                    withStyle(ChatFormatting.RED)
                }
        }
    }

    override fun serialize(): JsonElement {
        return JsonObject ().apply{
            addProperty("type", "global_reserve")
            addProperty("limit", limit)
            addProperty("sales", sales)
        }
    }

    companion object{
        fun deserialize(json : JsonObject) : GlobalReserve?{
            return try {
                GlobalReserve(
                    limit = json.get("limit").asInt,
                    sales = json.get("sales").asInt
                )
            }catch (e : Throwable){
                e.printStackTrace()
                null
            }
        }
    }
}

open class SimpleSingleReserve(
    open val limit : Int,
    val playerBought : MutableMap<UUID, Int> = mutableMapOf()
) : Reserve{
    override fun couldBuy(player: Player): Boolean {
        return playerBought.getOrDefault(player.uuid, 0) < limit
    }

    override fun consume(player: Player) {
        playerBought[player.uuid] = playerBought.getOrDefault(player.uuid, 0) + 1
    }

    override fun restock() {
        playerBought.clear()
    }

    override fun getTooltipComponent(player: Player): MutableComponent {
        return Component.translatable(
            "item.cobblemon_store.sell_menu.slot.reserve.simple_single_player",
            (limit - playerBought.getOrDefault(player.uuid, 0)),
            limit
        ).apply {
            if (limit <= playerBought.getOrDefault(player.uuid, 0)){
                withStyle(ChatFormatting.RED)
            }
        }
    }

    override fun serialize(): JsonElement {
        return JsonObject ().apply{
            addProperty("type", "simple_single_reserve")
            addProperty("limit", limit)
            add("player_bought", JsonArray().apply {
                playerBought.forEach {
                    add(JsonObject().apply{
                        addProperty("uuid", it.key.toString())
                        addProperty("value", it.value)
                    })
                }
            })
        }
    }

    companion object{
        fun deserialize(json : JsonObject) : SimpleSingleReserve?{
            return try {
                SimpleSingleReserve(
                    limit = json.get("limit").asInt,
                    playerBought = json.getAsJsonArray("player_bought").map { it.asJsonObject }.associate {
                        UUID.fromString(it.get("uuid").asString) to it.get("value").asInt
                    }.toMutableMap()
                )
            }catch (e : Throwable) {
                e.printStackTrace()
                null
            }
        }
    }
}