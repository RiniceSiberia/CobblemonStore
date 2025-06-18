package dev.windmill_broken.cobblemon_store.event

import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.Registrations
import dev.windmill_broken.cobblemon_store.bo.trade.ItemCostObj
import dev.windmill_broken.cobblemon_store.bo.trade.ItemPurchasingObj
import dev.windmill_broken.cobblemon_store.bo.trade.TradeSerializer
import dev.windmill_broken.money_lib.dao.json.JsonUtils.jsonConfig
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent

object StoreEvents {

    fun onTooltipsEvent(event : ItemTooltipEvent){
        val stack = event.itemStack
        val json = stack.components.get(Registrations.TagTypes.TRADE_ITEM_TAG.get())
        val trade = json?.let {
            try {
                jsonConfig.decodeFromString(TradeSerializer,it)
            }catch (e : Exception){
                CobblemonStore.Companion.LOGGER.error("deserialization err : ",e)
                null
            }
        }
        if (trade != null){
            if (trade.purchasing is ItemPurchasingObj){
                event.toolTip.add(
                    trade.cost.costToolTipComponent()
                )
            }else if (trade.cost is ItemCostObj){
                event.toolTip.add(
                    trade.purchasing.purchasingTooltipComponent()
                )
            }
            if (trade.storeLimits.isNotEmpty() && event.entity != null)
                trade.storeLimits.forEach {
                    event.toolTip.add(
                        it.value.getTooltipComponent(event.entity!!)
                    )
                }
        }
    }
}