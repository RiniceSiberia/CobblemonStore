package dev.windmill_broken.cobblemon_store.event

import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.Registrations
import dev.windmill_broken.cobblemon_store.bo.trade.ItemCost
import dev.windmill_broken.cobblemon_store.bo.trade.ItemPurchasing
import dev.windmill_broken.cobblemon_store.bo.trade.TradeSerializer
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.kJsonConfig
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent

object StoreEvents {

    fun onTooltipsEvent(event : ItemTooltipEvent){
        val stack = event.itemStack
        val json = stack.components.get(Registrations.TagTypes.TRADE_ITEM_TAG.get())
        val trade = json?.let {
            try {
                kJsonConfig.decodeFromString(TradeSerializer,it)
            }catch (e : Exception){
                CobblemonStore.Companion.LOGGER.error("deserialization err : ",e)
                null
            }
        }
        if (trade != null){
            if (trade.purchasing is ItemPurchasing){
                val tooltip = trade.cost.costToolTipComponent()
                if (tooltip != Component.empty()){
                    event.toolTip.add(tooltip)
                }
            }else if (trade.cost is ItemCost){
                event.toolTip.add(
                    trade.purchasing.purchasingTooltipComponent()
                )
            }
            if (trade.storeLimits.isNotEmpty() && event.entity != null)
                trade.storeLimits.forEach {
                    event.toolTip.add(
                        it.getTooltipComponent(event.entity!!)
                    )
                }
        }
    }
}