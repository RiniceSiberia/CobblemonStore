package dev.windmill_broken.cobblemon_store.event

import dev.windmill_broken.cobblemon_store.CobblemonStore.Companion.MOD_ID
import dev.windmill_broken.cobblemon_store.bo.trade.ItemCost
import dev.windmill_broken.cobblemon_store.bo.trade.ItemPurchasing
import dev.windmill_broken.cobblemon_store.bo.trade.MoneyCost
import dev.windmill_broken.cobblemon_store.bo.trade.MoneyPurchasing
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Items
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

object TestEvent {
    fun onTestEvent(event: PlayerInteractEvent.RightClickBlock){
        val p = event.entity
        if (p is ServerPlayer && DAOWharf.storeLibrary["test_store"] == null){
            DAOWharf.storeLibrary.create(
                id = "test_store",
                name = "test store",
                description = "test store",
                tradeValues = listOf(
                    Triple(
                        MoneyCost(5),
                        ItemPurchasing(Items.GOLD_NUGGET),
                        setOf()
                    ),
                    Triple(
                        ItemCost(Items.GOLD_INGOT),
                        MoneyPurchasing(45),
                        setOf()
                    )
                )
            )
        }
    }
}