package com.xxxt.cobblemon_store.event

import com.xxxt.cobblemon_store.Registrations
import com.xxxt.cobblemon_store.store.ItemCostObj
import com.xxxt.cobblemon_store.store.ItemPurchasingObj
import com.xxxt.cobblemon_store.store.Trade
import com.xxxt.cobblemon_store.utils.JsonFileUtils
import com.xxxt.cobblemon_store.utils.LOGGER
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent

object StoreEvents {

    fun onTooltipsEvent(event : ItemTooltipEvent){
        val stack = event.itemStack
        val json = stack.components.get(Registrations.TagTypes.TRADE_ITEM_TAG.get())
        val trade = json?.let {
            try {
                JsonFileUtils.jsonConfig.decodeFromString(Trade.serializer(),json)
            }catch (e : Exception){
                LOGGER.error("deserialization err : ",e)
                null
            }
        }
        if (trade != null){
            if (trade.purchasing is ItemPurchasingObj){
                if (trade.cost != null)
                    event.toolTip.add(
                        trade.cost.costToolTipComponent()
                    )
            }else if (trade.cost != null && trade.cost is ItemCostObj){
                event.toolTip.add(
                    trade.purchasing.purchasingTooltipComponent()
                )
            }
            if (trade.reserve != null && event.entity != null)
                event.toolTip.add(
                    trade.reserve.getTooltipComponent(event.entity!!)
                )
        }
    }
}