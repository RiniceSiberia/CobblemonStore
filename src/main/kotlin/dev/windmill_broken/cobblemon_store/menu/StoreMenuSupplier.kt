package dev.windmill_broken.cobblemon_store.menu

import com.cobblemon.mod.common.util.readString
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.neoforged.neoforge.network.IContainerFactory

object StoreMenuSupplier : IContainerFactory<StoreMenu> {
    override fun create(
        containerId: Int,
        inv: Inventory,
        buf: RegistryFriendlyByteBuf): StoreMenu {
        val pageIndex = buf.readInt()
        val store = DAOWharf.storeLibrary[buf.readString()]!!
        return StoreMenu(containerId,pageIndex,inv,store)
    }
}