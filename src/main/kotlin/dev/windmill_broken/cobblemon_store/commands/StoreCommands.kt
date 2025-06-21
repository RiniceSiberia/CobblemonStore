package dev.windmill_broken.cobblemon_store.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object StoreCommands {
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>){
        dispatcher.register(
            Commands.literal("cobblemon_store")
                .requires { source -> source.hasPermission(4) }
                .then(Commands.literal("add")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .then(Commands.argument("name", StringArgumentType.string()))
                        .executes { context ->
                            val id = context.getArgument("id", String::class.java)
                            val name = context.getArgument("name", String::class.java)
                            DAOWharf.storeLibrary.createOrUpdate(id,name)
                            1
                        }

                    )
                )
                .then(Commands.literal("remove"))
                .then(Commands.literal("rename"))
                .then(Commands.literal("list"))
        )
    }
}