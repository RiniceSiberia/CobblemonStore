import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class StoreSelectionScreen(title: Component) : Screen(title) {

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
//        guiGraphics.renderItem(ItemStack(Items.GOLD_INGOT), mouseX, mouseY)
        addRenderableWidget(EditBox(Minecraft.getInstance().font, 30, 30, this.width / 2, 20, Component.empty()))
        addRenderableWidget(Button.builder(Component.literal("保存")){
            Minecraft.getInstance().player?.sendSystemMessage(Component.literal("test"))
        }.pos(this.width/2,this.height/2+30).build())
        // submit

    }
}
