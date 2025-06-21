package dev.windmill_broken.cobblemon_store.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object StoreCommands {
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>){
        dispatcher.register(
            Commands.literal("cobblemon_store")
                .requires { source -> source.hasPermission(4) }
                .then(Commands.literal("store")
                    .then(Commands.literal("create_or_update")
                        .then(Commands.argument("id", StringArgumentType.string())
                            .then(Commands.argument("name", StringArgumentType.string()))
                            .executes { context ->
                                val id = context.getArgument("id", String::class.java)
                                val name = context.getArgument("name", String::class.java)
                                DAOWharf.storeLibrary.createOrUpdate(id,name)
                                context.source.player?.apply {
                                    this.displayClientMessage(
                                        Component.literal(
                                            "add store $id:$name success!"
                                        ),true
                                    )
                                }
                                1
                            }
                        )
                    ).then(Commands.literal("list")
                        .executes { context ->
                            val list = DAOWharf.storeLibrary.list()
                            context.source.player?.apply {
                                this.displayClientMessage(
                                    Component.literal(
                                        "this is all stores:\n${list.joinToString("\n") { "$id:$name" }}"
                                    ),true
                                )
                            }
                            1
                        }
                    ).then(Commands.literal("remove")
                        .then(Commands.argument("id", StringArgumentType.string())
                            .executes { context ->
                                DAOWharf.storeLibrary.removeById(context.getArgument("id", String::class.java))
                                1
                            }
                        )
                    )
                )
        )
    }
}