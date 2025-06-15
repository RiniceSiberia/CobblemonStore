package com.xxxt.cobblemon_store.store

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player
import java.util.*


/**
 * 实现该接口为有不同程度上的存货上限
 */
sealed interface Reserve{

    val type : String

    fun couldBuy(player: Player) : Boolean

    fun consume(player: Player)

    fun restock()

    fun getTooltipComponent(player: Player) : MutableComponent

    fun serialize(): JsonObject{
        return JsonObject().apply {
            addProperty("name","reserve")
            addProperty("type",type)
            serialize()
        }
    }

    fun JsonObject.serialize()

    companion object{
        fun deserialize(json : JsonObject) : Reserve?{
            return try {
                val name = json.get("name").asString
                assert(name == "reserve")
                val type = json.get("type").asString
                when (type) {
                    "global" -> GlobalReserve.deserialize(json)
                    "simple_single_player" -> SimpleSinglePlayerReserve.deserialize(json)
                    else -> null
                }
            }catch (e : Throwable){
                e.printStackTrace()
                null
            }
        }
    }
}

class GlobalReserve(
    val limit : Int,
    var sales : Int = 0,
//    val identity : Set<StoreIdentity> = emptySet()
) : Reserve{

    override val type: String
        get() = "global"

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

    override fun JsonObject.serialize() {
        addProperty("limit", limit)
        addProperty("sales", sales)
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

open class SimpleSinglePlayerReserve(
    open val limit : Int,
    val playerBought : MutableMap<UUID, Int> = mutableMapOf()
) : Reserve{

    override val type: String
        get() = "simple_single_player"

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

    override fun JsonObject.serialize() {
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

    companion object{
        fun deserialize(json : JsonObject) : SimpleSinglePlayerReserve?{
            return try {
                SimpleSinglePlayerReserve(
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