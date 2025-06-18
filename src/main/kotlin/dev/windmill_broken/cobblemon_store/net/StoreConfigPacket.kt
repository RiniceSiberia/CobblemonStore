package dev.windmill_broken.cobblemon_store.net

import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class StoreConfigPacket(
    val pos: BlockPos,
    val storeId: String
) : CustomPacketPayload {

    companion object {
        val TYPE = CustomPacketPayload.Type<StoreConfigPacket>(
            ResourceLocation.fromNamespaceAndPath("cobblemon_store", "store_config")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, StoreConfigPacket> = StreamCodec.composite(
            BlockPos.STREAM_CODEC, StoreConfigPacket::pos,
            ByteBufCodecs.STRING_UTF8, StoreConfigPacket::storeId,
            ::StoreConfigPacket
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE
}