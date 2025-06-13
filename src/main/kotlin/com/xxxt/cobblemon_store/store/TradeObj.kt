@file:UseSerializers(
    UUIDSerializer::class,
    ItemStackSerializer::class
)
package com.xxxt.cobblemon_store.store

import com.xxxt.cobblemon_store.utils.ItemStackSerializer
import com.xxxt.cobblemon_store.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.*

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
            purchasing.payComponent().also {
                if (cost!=null)
                    it.append(cost.costMsgComponent())
            }
        )
        return true
    }

    fun removeIt(){
        supStore?.trades?.remove(this)
    }

    fun getShowedItemStack(player: Player): ItemStack {
            if (purchasing is ItemPurchasingObj){
                return purchasing.stack.copy().also {
                    it.set(DataComponents.CUSTOM_NAME,
                        Component.translatable(
                            "item.cobblemon_store.sell_menu.slot.name",
                            Component.translatable("item.cobblemon_store.sell_menu.slot.sell"),
                            purchasing.stack.displayName
                        ).withStyle(ChatFormatting.GOLD).also { it ->
                            if (cost!=null){
                                it.append(
                                    cost.costToolTipComponent()
                                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                                )
                            }
                            if (reserve != null){
                                it.append(
                                    reserve.getTooltipComponent(player)
                                )
                            }
                        }
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
                }

            }
            return ItemStack.EMPTY
        }
}


/**
 * 实现该接口为有不同程度上的存货上限
 */
@Serializable
sealed interface Reserve{
    fun couldBuy(player: Player) : Boolean

    fun consume(player: Player)

    fun restock()

    fun getTooltipComponent(player: Player) : MutableComponent
}

@Serializable
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
            ).also {
                if (sales >= limit){
                    it.withStyle(ChatFormatting.RED)
                }
        }
    }

}

@Serializable
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
        ).also {
            if (limit <= playerBought.getOrDefault(player.uuid, 0)){
                it.withStyle(ChatFormatting.RED)
            }
        }
    }
}