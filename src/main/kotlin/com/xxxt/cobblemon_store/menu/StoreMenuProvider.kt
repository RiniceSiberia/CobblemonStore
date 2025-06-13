package com.xxxt.cobblemon_store.menu

import com.xxxt.cobblemon_store.store.Store
import net.minecraft.network.chat.Component
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu

data class StoreMenuProvider(
    val createMenu : (Int, Inventory, Player) -> AbstractContainerMenu,
    val store : Store,
    val pageIndex : Int = 0
) :MenuProvider{
    override fun createMenu(
        containerId: Int,
        inv: Inventory,
        player: Player
    ): AbstractContainerMenu? {

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