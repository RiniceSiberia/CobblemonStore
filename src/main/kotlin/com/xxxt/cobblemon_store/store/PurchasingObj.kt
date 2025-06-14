package com.xxxt.cobblemon_store.store

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import com.xxxt.cobblemon_store.CobblemonStore.Companion.LOGGER
import com.xxxt.cobblemon_store.utils.PluginUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.ItemHandlerHelper
import kotlin.reflect.KClass

sealed class PurchasingObj<T: Any>{

    abstract val clazz : KClass<out T>

    abstract fun purchasing(player : Player)

    abstract fun purchasingMsgComponent() : MutableComponent

    abstract fun purchasingTooltipComponent() : MutableComponent

    abstract fun serialize() : JsonElement

    companion object{
        fun deserialize(json : JsonObject) : PurchasingObj<*>?{
            if (json.has("type") && json.get("type").isJsonPrimitive){
                val type = json.get("type").asString
                when (json.asJsonObject.get("type").asString) {
                    "item_purchasing" -> return ItemPurchasingObj.deserialize(json)
                    "money_purchasing" -> return MoneyPurchasingObj.deserialize(json)
                }
            }
            return null
        }
    }
}

open class ItemPurchasingObj(
    open val stack : ItemStack
) : PurchasingObj<ItemStack>(){
    override val clazz: KClass<out ItemStack>
        get() = stack::class

    override fun purchasing(player: Player) {
        ItemHandlerHelper.giveItemToPlayer(player,stack)
    }

    override fun purchasingMsgComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.purchasing.item",stack.hoverName,stack.count)
    }

    override fun purchasingTooltipComponent(): MutableComponent {
        return Component.translatable(
            "msg.cobblemon_store.slot.purchasing.item",
            stack.displayName,
            stack.count
        )
    }

    override fun serialize(): JsonElement {
        return JsonObject().apply {
            addProperty("type","item_purchasing")
            val nbt = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE,this@ItemPurchasingObj.stack)
                .resultOrPartial{err ->
                    LOGGER.error("item stack序列化出错:${err}")
                }.orElseThrow()
            add("nbt",nbt)
        }
    }

    companion object{
        fun deserialize(json : JsonObject) : ItemPurchasingObj?{
            return try {
                val stack = ItemStack.CODEC.parse(JsonOps.INSTANCE,json.getAsJsonObject("nbt"))
                    .resultOrPartial{err ->
                        LOGGER.error("item stack反序列化出错:${err}")
                    }.orElseThrow()
                ItemPurchasingObj(stack)
            }catch (e : Throwable){
                e.printStackTrace()
                null
            }
        }
    }
}

class MoneyPurchasingObj(
    val value : Double
) : PurchasingObj<Double>(){
    override val clazz: KClass<out Double>
        get() = Double::class

    override fun purchasing(player: Player) {
        PluginUtils.addMoney(player, value)
    }

    override fun purchasingMsgComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.purchasing.gts_money", String.format("%.2f", value))
    }

    override fun purchasingTooltipComponent(): MutableComponent {
        return Component.translatable(
            "msg.cobblemon_store.slot.purchasing.gts_money",
            String.format("%.2f", value)
        )
    }


    override fun serialize(): JsonElement {
        return JsonObject().apply {
            addProperty("type","money_purchasing")
            addProperty("value",value)
        }
    }

    companion object{
        fun deserialize(json : JsonObject) : MoneyPurchasingObj?{
            return try {
                val value = json.getAsJsonPrimitive("value").asDouble
                MoneyPurchasingObj(value)
            }catch (e : Throwable){
                e.printStackTrace()
                null
            }
        }
    }
}