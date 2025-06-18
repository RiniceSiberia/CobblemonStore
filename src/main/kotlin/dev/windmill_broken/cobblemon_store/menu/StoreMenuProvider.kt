package dev.windmill_broken.cobblemon_store.menu

import dev.windmill_broken.cobblemon_store.bo.store.Store
import net.minecraft.network.chat.Component
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player

data class StoreMenuProvider(
    val store : Store,
    val pageIndex : Int
) :MenuProvider{
    override fun createMenu(
        containerId: Int,
        inv: Inventory,
        player: Player
    ): StoreMenu {
        return StoreMenu(
            containerId = containerId,
            pageIndex = pageIndex,
            playerInventory = inv,
            store = store
        )
    }

    override fun getDisplayName(): Component
    = Component.translatable("menu.cobblemon_store.store")

}