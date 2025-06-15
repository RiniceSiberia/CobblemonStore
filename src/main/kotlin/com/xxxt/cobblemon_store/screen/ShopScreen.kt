package com.xxxt.cobblemon_store.screen

import com.xxxt.cobblemon_store.menu.StoreMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

class ShopScreen(menu: StoreMenu, playerInventory: Inventory, title: Component) :
    AbstractContainerScreen<StoreMenu>(menu, playerInventory, title) {
    private val location: ResourceLocation =
        ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png")
    private var containerRows = 0

    init {
        super.init()
        this.containerRows = StoreMenu.ROW
        this.imageHeight = 114 + this.containerRows * 18
        this.inventoryLabelY = this.imageHeight - 94
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        this.renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun renderLabels(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int) {
        pGuiGraphics.drawString(
            this.font,
            this.title,
            this.titleLabelX + this.imageWidth - 60,
            this.titleLabelY, 0x404040, true
        )
        pGuiGraphics.drawString(
            this.font,
            this.playerInventoryTitle,
            this.inventoryLabelX,
            this.inventoryLabelY, 0x404040, true
        )
    }

    override fun renderBg(guiGraphics: GuiGraphics, p1: Float, p2: Int, p3: Int) {
        val x = (this.width - this.imageWidth) / 2
        val y = (this.height - this.imageHeight) / 2
        guiGraphics.blit(
            location,
            x,
            y,
            0,
            0,
            this.imageWidth,
            this.containerRows * 18 + 17
        )
        guiGraphics.blit(
            location, x, y + this.containerRows * 18 + 17, 0, 126,
            this.imageWidth, 96
        )
    }
}