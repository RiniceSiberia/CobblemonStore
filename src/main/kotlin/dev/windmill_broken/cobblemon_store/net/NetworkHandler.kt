package dev.windmill_broken.cobblemon_store.net

import dev.windmill_broken.cobblemon_store.net.StoreConfigPacket
import dev.windmill_broken.cobblemon_store.block.StoreBlockEntity
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.handling.IPayloadContext

object NetworkHandler {

    fun handleStoreConfigChanged(packet: StoreConfigPacket, context: IPayloadContext) {
        val player = context.player() as? ServerPlayer ?: return

        // 验证玩家权限
        if (!player.isCreative || !player.hasPermissions(2)) {
            return
        }
        if (context.flow().isServerbound) {
            context.enqueueWork {
                val level = player.serverLevel()
                val blockEntity = level.getBlockEntity(packet.pos) as? StoreBlockEntity ?: return@enqueueWork

                // 更新BlockEntity数据
                blockEntity.storeId = packet.storeId
                blockEntity.setChanged()
            }
        }

    }


}