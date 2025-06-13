package com.xxxt.cobblemon_store.screen

import com.xxxt.cobblemon_store.store.Store
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.font.TextFieldHelper
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.EnderChestBlock
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
class StoreChoosingScreen(
    val store : Store? = null,
) : Screen(
    Component.translatable("screen.cobblemon_store.choosing.title")
){
    var chooseField : EditBox = EditBox(
        Minecraft.getInstance().font,
        width / 2 - 100,
        height / 4 - 50,
        200,
        20,
        Component.translatable("screen.cobblemon_store.choosing.search")
    )
        private set

    val storeNameField : EditBox = Button(
        Minecraft.getInstance().font,
        width / 2 - 100,
        height / 4 - 30,
        200,
        20,
        Component.translatable("screen.cobblemon_store.choosing.name"
    )

    private val saveButton : Button
    private val cancelButton : Button

    init {
        super.init()
        val oldInput = store?.id
            storeNameField

    }
}