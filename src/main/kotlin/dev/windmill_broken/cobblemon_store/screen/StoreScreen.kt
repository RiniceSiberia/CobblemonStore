package dev.windmill_broken.cobblemon_store.screen

import dev.windmill_broken.cobblemon_store.menu.StoreMenu
import dev.windmill_broken.cobblemon_store.menu.StoreMenuProvider
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

class StoreScreen(
    menu: StoreMenu,
    playerInventory: Inventory,
    title: Component
) : AbstractContainerScreen<StoreMenu>(menu, playerInventory, title) {

    private val location: ResourceLocation =
        ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png")
    private var containerRows = 0


    init {
        super.init()
        this.containerRows = StoreMenu.Companion.ROW
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
            this.titleLabelX,
            this.titleLabelY, 0x636363, false
        )
        pGuiGraphics.drawString(
            this.font,
            this.playerInventoryTitle,
            this.inventoryLabelX,
            this.inventoryLabelY, 0x636363, false
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

    companion object{
        const val BUTTON_SIZE = 16
    }
}