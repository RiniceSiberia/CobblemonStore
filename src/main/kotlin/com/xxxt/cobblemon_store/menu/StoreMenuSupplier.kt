package com.xxxt.cobblemon_store.menu

import com.xxxt.cobblemon_store.store.Store
import com.xxxt.cobblemon_store.store.StoresLibrary
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.MenuType
import net.neoforged.neoforge.network.IContainerFactory

object StoreMenuSupplier : IContainerFactory<StoreMenu> {
    override fun create(
        containerId: Int,
        inv: Inventory,
        buf: RegistryFriendlyByteBuf): StoreMenu {
        val pageIndex = buf.readInt()
        val store = StoresLibrary[buf.readInt()]!!
        return StoreMenu(containerId,pageIndex,inv,store)
    }
}