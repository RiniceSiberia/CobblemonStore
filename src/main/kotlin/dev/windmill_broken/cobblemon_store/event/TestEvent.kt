package dev.windmill_broken.cobblemon_store.event

import dev.windmill_broken.cobblemon_store.bo.trade.SimpleItemCost
import dev.windmill_broken.cobblemon_store.bo.trade.ItemStackPurchasing
import dev.windmill_broken.cobblemon_store.bo.trade.MoneyCost
import dev.windmill_broken.cobblemon_store.bo.trade.MoneyPurchasing
import dev.windmill_broken.cobblemon_store.bo.trade.ServerTradeCreator
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Items
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

object TestEvent {
    fun onTestEvent(event: PlayerInteractEvent.RightClickBlock){
        val p = event.entity
        if (p is ServerPlayer){
            if(DAOWharf.storeLibrary["test_store"] == null){
                DAOWharf.storeLibrary.create(
                    id = "test_store",
                    name = "test store",
                    description = "test store",
                )
            }
            DAOWharf.tradeLibrary.createTrade(
                "test_store",
                ServerTradeCreator,
                MoneyCost(5),
                ItemStackPurchasing(Items.GOLD_NUGGET),
                setOf()
            )
            DAOWharf.tradeLibrary.createTrade(
                "test_store",
                ServerTradeCreator,
                SimpleItemCost(BuiltInRegistries.ITEM.getKey(Items.GOLD_INGOT)),
                MoneyPurchasing(45),
                setOf()
            )
        }
    }
}