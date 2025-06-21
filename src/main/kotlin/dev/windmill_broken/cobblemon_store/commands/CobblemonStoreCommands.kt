package dev.windmill_broken.cobblemon_store.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object CobblemonStoreCommands {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>, registry: CommandBuildContext, selection: Commands.CommandSelection) {
        StoreCommands.register(dispatcher)
        TradeCommands.register(dispatcher)
    }
}