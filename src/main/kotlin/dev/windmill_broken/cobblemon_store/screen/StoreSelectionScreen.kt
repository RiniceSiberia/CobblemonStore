package dev.windmill_broken.cobblemon_store.screen

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
    title: Component
) : Screen(title) {
        // 创建输入框
    companion object{
        const val SAVE_BUTTON_WIDTH = 100
        const val SAVE_BUTTON_HEIGHT = 20
    }




    override fun init() {
        super.init()
        val storeIdBox: EditBox = EditBox(
            Minecraft.getInstance().font,
            this.width / 2 - 100,
            this.height / 2 - 40,
            200,
            20,
            Component.translatable("screen.cobblemon_store.choosing.store_id")
        ).also {
            it.value = storeBlockEntity.storeId ?: "" // 设置当前值
        }
        // 创建保存按钮
        val saveButton: Button = Button.builder(Component.translatable("screen.cobblemon_store.choosing.save")) { button ->
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
        }.pos((this.width-SAVE_BUTTON_WIDTH)/2, (this.height +SAVE_BUTTON_HEIGHT)/2)
            .size(SAVE_BUTTON_WIDTH, SAVE_BUTTON_HEIGHT).build()
        addRenderableWidget(storeIdBox)
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
