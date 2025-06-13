@file:UseSerializers(
    UUIDSerializer::class,
    ItemStackSerializer::class
)
package com.xxxt.cobblemon_store.store

import com.xxxt.cobblemon_store.Registrations
import com.xxxt.cobblemon_store.utils.ItemStackSerializer
import com.xxxt.cobblemon_store.utils.JsonFileUtils
import com.xxxt.cobblemon_store.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

@Serializable
class Trade(
    val storeIndex : Int,
    val cost : CostObj<*>?,
    val purchasing : PurchasingObj<*>,
    val reserve : Reserve?
){
    val supStore
        get() = StoresLibrary[storeIndex]

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
        purchasing.purchasing(player)
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
                    it.set(DataComponents.CUSTOM_NAME,
                        Component.translatable(
                            "item.cobblemon_store.sell_menu.slot.name",
                            Component.translatable("item.cobblemon_store.sell_menu.slot.sell"),
                            purchasing.stack.displayName
                        ).withStyle(ChatFormatting.GOLD)
                    )
                    it.set(
                        Registrations.TagTypes.TRADE_ITEM_TAG,
                        JsonFileUtils.jsonConfig
                            .encodeToString( this )
                        )
                }
            }else if (cost is ItemCostObj){
                return cost.stack.copy().also {
                    it.set(DataComponents.CUSTOM_NAME,
                        Component.translatable(
                            "item.cobblemon_store.sell_menu.slot.name",
                            Component.translatable("item.cobblemon_store.sell_menu.slot.buy"),
                            Component.translatable(
                                "item.cobblemon_store.sell_menu.slot.gts_money",
                                purchasing
                            )
                        )
                    )
                    it.set(
                        Registrations.TagTypes.TRADE_ITEM_TAG,
                        JsonFileUtils.jsonConfig
                            .encodeToString( this )
                    )
                }

            }
            return ItemStack.EMPTY
        }
}
