package com.xxxt.cobblemon_store.screen

import dev.windmill_broken.cobblemon_store.net.StoreConfigPacket
import dev.windmill_broken.cobblemon_store.block.StoreBlockEntity
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.network.PacketDistributor

class StoreSelectionScreen(
    var storeBlockEntity: StoreBlockEntity,
    title: Component,
) : Screen(title) {
    private lateinit var storeIdBox: EditBox
    private lateinit var saveButton: Button
    override fun init() {
        super.init()

        // 创建输入框 - 在init中创建而不是render中
        storeIdBox = EditBox(
            Minecraft.getInstance().font,
            this.width / 2 - 100,
            this.height / 2 - 40,
            200,
            20,
            Component.literal("Store ID")
        )

        storeIdBox.value = storeBlockEntity.storeId ?: "" // 设置当前值

        addRenderableWidget(storeIdBox)

        // 创建保存按钮
        saveButton = Button.builder(Component.literal("保存")) { button ->
            if (button.active && storeIdBox.value.isNotEmpty()) {
                // 发送网络包到服务端而不是直接修改
                PacketDistributor.sendToServer(
                    StoreConfigPacket(
                        storeBlockEntity.blockPos,
                        storeIdBox.value
                    )
                )
                this.onClose()
            }
        }.pos(this.width / 2 - 50, this.height / 2 + 10).size(100, 20).build()

        addRenderableWidget(saveButton)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        // 渲染背景
//        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        // 渲染标题
        guiGraphics.drawCenteredString(
            this.font,
            this.title,
            this.width / 2,
            this.height / 2 - 80,
            0xFFFFFF
        )

        // 渲染标签
        guiGraphics.drawString(
            this.font,
            "Store ID:",
            this.width / 2 - 100,
            this.height / 2 - 55,
            0xFFFFFF
        )

    }

    override fun isPauseScreen(): Boolean {
        return false
    }

}
