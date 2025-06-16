
package com.xxxt.cobblemon_store.store

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import com.xxxt.cobblemon_store.CobblemonStore.Companion.LOGGER
import com.xxxt.cobblemon_store.utils.PluginUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.util.*
import kotlin.math.max

sealed class CostObj {

    abstract val type : TradeType

    abstract fun enough(player: Player): Boolean

    abstract fun pay(player: Player) : Boolean

    abstract fun costMsgComponent() : MutableComponent

    abstract fun costToolTipComponent() : MutableComponent

    fun serialize() : JsonElement{
        return JsonObject().apply {
            addProperty("name","cost")
            addProperty("type",type.lowercaseName)
            serialize()
        }
    }

    abstract fun JsonObject.serialize()

    companion object{
        fun deserialize(json: JsonObject) : CostObj?{
            return try {
                val name = json.get("name").asString
                assert(name == "cost")
                val type = json.asJsonObject.get("type").asString
                when(type){
                    TradeType.MONEY.lowercaseName ->{
                        MoneyCostObj.deserialize(json)
                    }
                    TradeType.ITEM.lowercaseName ->{
                        ItemCostObj.deserialize(json)
                    }
                    TradeType.POKEMON.lowercaseName ->{
                        null
                    }
                    else -> null
                }
            }catch (e : Throwable){
                e.printStackTrace()
                null
            }
        }
    }
}

class MoneyCostObj(
    val value : Double
) : CostObj(){

    init {
        if (value.isNaN() || value.isInfinite()){
            0.0
        }else{
            value
        }
    }

    override val type: TradeType
        get() = TradeType.MONEY

    override fun enough(player: Player): Boolean {
        return if (player is ServerPlayer && PluginUtils.checkBukkitInstalled()){
            val wallet = PluginUtils.getValut(player)?:return false
            wallet >= value
        }else{
            false
        }
    }

    override fun pay(player: Player): Boolean {
        if (player.isCreative) return true
        return PluginUtils.minusMoney(player,value)
    }

    override fun costMsgComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.cost.money",String.format("%.2f", value))
    }

    override fun costToolTipComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.slot.cost.money",String.format("%.2f", value))
    }

    override fun JsonObject.serialize() {
        addProperty("value",value)
    }

    companion object{
        fun deserialize(json : JsonObject) : MoneyCostObj?{
            return try {
                val price = json.getAsJsonPrimitive("value").asDouble
                MoneyCostObj(price)
            }catch (e : Throwable){
                e.printStackTrace()
                null
            }
        }
    }
}

/**
 * 不支持nbt检测
 */
class ItemCostObj(
    val stack: ItemStack,
) : CostObj(){

    override val type: TradeType
        get() = TradeType.ITEM

    override fun enough(player: Player): Boolean {
        return player.inventory.items.any {
            it.item == stack.item
        }
    }

    override fun pay(player: Player): Boolean {
        if (player.isCreative) return true

        val targets = player.inventory.items.filter {
            it.item == stack.item
        }
        if (targets.sumOf { it.count } < stack.count) {
            player.sendSystemMessage(Component.translatable("msg.cobblemon_store.not_enough_money"))
            return false
        }
        var countVariable = stack.count
        var repeatNum = 0
        while(countVariable>= 0){
            val target = targets.firstOrNull{it.count > 0}?:return false.also {
                player.sendSystemMessage(
                    Component.translatable("msg.cobblemon_store.err", Calendar.getInstance())
                )
                LOGGER.error(
                    "Player ${player.name} attempted to pay $countVariable of ${this.stack.displayName}," +
                            " but only ${targets.sumOf { it.count }} were available in inventory.")
            }
            val shrinkCount = max(countVariable,target.count)
            target.shrink(shrinkCount)
            countVariable -= shrinkCount
            repeatNum++
            if (repeatNum >= 1000){
                Component.translatable("msg.cobblemon_store.err", Calendar.getInstance())
                LOGGER.error("Aborting loop: exceeded maximum allowed iterations (100000). Possible infinite loop.")
            }
        }
        return true
    }

    override fun costMsgComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.cost.item", stack.hoverName,stack.count)
    }

    override fun costToolTipComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.slot.cost.item", stack.hoverName,stack.count)
    }

    override fun JsonObject.serialize() {
        val nbt = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE,this@ItemCostObj.stack)
            .resultOrPartial{err ->
                LOGGER.error("item stack序列化出错:${err}")
            }.orElseThrow()
        add("nbt",nbt)
    }

    companion object{
        fun deserialize(json : JsonObject) : ItemCostObj?{
            return try {
                val stack = ItemStack.CODEC.parse(JsonOps.INSTANCE,json.getAsJsonObject("nbt"))
                    .resultOrPartial{err ->
                        LOGGER.error("item stack反序列化出错:${err}")
                    }.orElseThrow()
                ItemCostObj(stack)
            }catch (e : Throwable){
                e.printStackTrace()
                null
            }
        }
    }
}
