package dev.windmill_broken.cobblemon_store.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import dev.windmill_broken.cobblemon_store.CobblemonStore.Companion.LOGGER
import dev.windmill_broken.cobblemon_store.bo.trade.Trade
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.kJsonConfig
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object TradeCommands {
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>){
        dispatcher.register(
            Commands.literal("cobblemon_store")
                .requires { source -> source.hasPermission(4) }
                .then(Commands.literal("trade")
                    .then(Commands.literal("import_json")
                        .then(Commands.argument("json", StringArgumentType.string())
                            .executes { context ->
                                try {
                                    val json = context.getArgument("json", String::class.java)
                                    val maps = kJsonConfig.decodeFromString(
                                        MapSerializer(Int.serializer(), Trade.serializer()),
                                        json
                                    )
                                    DAOWharf.tradeLibrary.import(
                                        maps.values
                                    )
                                    context.source.sendSystemMessage(
                                        Component.literal(
                                            "Import success!"
                                        )
                                    )
                                    1
                                }catch (e : Exception){
                                    LOGGER.error("Error while parsing json", e)
                                    context.source.sendSystemMessage(
                                        Component.literal(
                                            "Error while parsing json"
                                        )
                                    )
                                    0
                                }
                            }))
                    ))
    }
}