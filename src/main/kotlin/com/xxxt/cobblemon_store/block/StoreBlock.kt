package com.xxxt.cobblemon_store.block

import com.cobblemon.mod.common.util.writeString
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.xxxt.cobblemon_store.menu.StoreMenuProvider
import com.xxxt.cobblemon_store.screen.StoreSelectionScreen
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import net.minecraft.world.phys.BlockHitResult

class StoreBlock(
    properties: Properties
) : BaseEntityBlock(
    properties
        .mapColor(MapColor.QUARTZ)
        .instrument(NoteBlockInstrument.GUITAR)
        .strength(-1f, 3600000.0f)
        .pushReaction(PushReaction.IGNORE)
        .noLootTable()
        .isValidSpawn(Blocks::never)
        .sound(SoundType.EMPTY).ignitedByLava()
) {
    private val CODEC: MapCodec<StoreBlock> =
        RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<StoreBlock> ->
            instance.group(
                propertiesCodec()
            ).apply(
                instance
            ) { properties: Properties ->
                StoreBlock(properties)
            }
        }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (!level.isClientSide()) {
            val be = level.getBlockEntity(pos)
            if (be != null && be is StoreBlockEntity) {
                val store = be.store
                if (store != null) {
                    player.openMenu(
                        StoreMenuProvider(store, 0)
                    )
                    { buf ->
                        buf.writeInt(0)
                        buf.writeString(store.id)
                    }
                    return InteractionResult.CONSUME
                }
            }
        } else {
            if (player.isCreative && player.isCrouching) {
                val be = level.getBlockEntity(pos)
                if (be != null && be is StoreBlockEntity) {
                    Minecraft.getInstance().setScreen(
                        StoreSelectionScreen(
                            be,
                            Component.literal("Admin Setting Screen"),
                        )
                    )
                }

            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun codec(): MapCodec<out BaseEntityBlock?> {
        return CODEC
    }

    override fun newBlockEntity(
        p0: BlockPos,
        p1: BlockState
    ): StoreBlockEntity {
        return StoreBlockEntity(p0, p1)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }
}