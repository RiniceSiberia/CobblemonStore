package dev.windmill_broken.cobblemon_store.block

import dev.windmill_broken.cobblemon_store.Registrations
import dev.windmill_broken.cobblemon_store.bo.store.Store
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class StoreBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(
    Registrations.BlockEntities.STORE_BLOCK_ENTITY_TYPE.get(),
    pos,
    state
) {
    var storeId: String? = null

    val store : Store?
        get() = storeId?.let { DAOWharf.storeLibrary[it] }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        if (storeId != null) {
            tag.putString("store_id_tag", storeId!!)
        }
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        storeId = if (tag.contains("store_id_tag")) {
            tag.getString("store_id_tag")
        } else {
            null
        }
    }
}