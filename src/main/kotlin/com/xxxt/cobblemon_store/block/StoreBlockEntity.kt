package com.xxxt.cobblemon_store.block

import com.xxxt.cobblemon_store.Registrations
import com.xxxt.cobblemon_store.store.Store
import com.xxxt.cobblemon_store.store.StoresLibrary
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class StoreBlockEntity(
    pos : BlockPos,
    state : BlockState
) : BlockEntity(
    Registrations.BlockEntities.STORE_BLOCK_ENTITY_TYPE,
    pos,
    state
){
    var storeId : String? = null

    val store : Store?
        get() = StoresLibrary[storeId]

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.putString("store_id_tag",storeId)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        storeId = tag.getString("store_id_tag")
    }

}