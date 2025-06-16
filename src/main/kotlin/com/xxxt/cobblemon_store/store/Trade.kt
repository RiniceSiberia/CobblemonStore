package com.xxxt.cobblemon_store.store

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.xxxt.cobblemon_store.Registrations
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class Trade(
    val storeId : String,
    val cost : CostObj?,
    val purchasing : PurchasingObj,
    val reserve : Reserve?
){
    val supStore
        get() = StoresLibrary[storeId]

    fun trade( player: Player) : Boolean{
        if (cost != null && !cost.enough(player)){
            player.sendSystemMessage(Component.translatable("msg.cobblemon_store.not_enough_money"))
            return false
        }
        if (reserve != null && !reserve.couldBuy(player)){
            player.sendSystemMessage(Component.translatable("msg.cobblemon_store.sold_out"))
            return false
        }
        cost?.pay(player)?.also { if(!it) return false }
        reserve?.consume(player)
        val warehouse = WarehouseLibrary.getOrCreate(player)
        val purchasingItem = purchasing.purchasing(player)
        warehouse.put(purchasingItem.index,purchasingItem)
        player.sendSystemMessage(
            purchasing.purchasingMsgComponent().also {
                if (cost!=null)
                    it.append(cost.costMsgComponent())
            }
        )
        return true
    }

    fun removeIt(){
        supStore?.trades?.remove(this)
    }

    val showedItemStack : ItemStack
        get(){
            if (purchasing is ItemPurchasingObj){
                return purchasing.stack.copy().also {
                    it[DataComponents.CUSTOM_NAME] = Component.translatable(
                        "item.cobblemon_store.sell_menu.slot.name",
                        Component.translatable("item.cobblemon_store.sell_menu.slot.sell"),
                        purchasing.stack.displayName
                    ).withStyle(ChatFormatting.GOLD)
                    it.set(
                        Registrations.TagTypes.TRADE_ITEM_TAG,
                        Gson().toJson(serialize())
                    )
                }
            }else if (cost is ItemCostObj){
                return cost.stack.copy().also {
                    it.set(DataComponents.CUSTOM_NAME,
                        Component.translatable(
                            "item.cobblemon_store.sell_menu.slot.name",
                            Component.translatable("item.cobblemon_store.sell_menu.slot.buy"),
                            Component.translatable(
                                "item.cobblemon_store.sell_menu.slot.money",
                                purchasing
                            )
                        )
                    )
                    it.set(
                        Registrations.TagTypes.TRADE_ITEM_TAG,
                        Gson().toJson(serialize())
                    )
                }

            }
            return ItemStack.EMPTY
        }

    fun serialize() : JsonObject {
        return JsonObject().apply {
            addProperty("store_id",storeId)
            add("cost",cost?.serialize()?:NULL_HOLDER)
            add("purchasing",purchasing.serialize())
            add("reserve",reserve?.serialize()?:NULL_HOLDER)
        }
    }

    companion object{

        val NULL_HOLDER : JsonElement = JsonPrimitive(-1)

        fun deserialize(json: JsonObject) : Trade?{
            return try {
                val cost = json.get("cost").asJsonObject
                val costNullFlag = cost == NULL_HOLDER
                val purchasing = json.get("purchasing").asJsonObject
                val reserve = json.get("reserve").asJsonObject
                val reverseNullFlag = reserve == NULL_HOLDER
                Trade(
                    json.get("store_id").asString,
                    if (!costNullFlag) CostObj.deserialize(cost)!! else null,
                    PurchasingObj.deserialize(purchasing)!!,
                    if (!reverseNullFlag) Reserve.deserialize(reserve)!! else null
                )
            }catch (e : Throwable){
                e.printStackTrace()
                null
            }
        }
    }
}
