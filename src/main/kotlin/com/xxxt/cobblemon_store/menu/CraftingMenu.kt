package com.xxxt.cobblemon_store.menu

import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.RecipeBookMenu
import net.minecraft.world.inventory.RecipeBookType
import net.minecraft.world.inventory.ResultContainer
import net.minecraft.world.inventory.ResultSlot
import net.minecraft.world.inventory.Slot
import net.minecraft.world.inventory.TransientCraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks

class CraftingMenu(
    containerId: Int,
    playerInventory: Inventory,
    private val access: ContainerLevelAccess = ContainerLevelAccess.NULL
) : RecipeBookMenu<CraftingInput?, CraftingRecipe?>(MenuType.CRAFTING, containerId) {

    private val craftSlots: CraftingContainer = TransientCraftingContainer(this, 3, 3)
    private val resultSlots: ResultContainer = ResultContainer()
    private val player: Player = playerInventory.player
    private var placingRecipe = false

    init {
        this.addSlot(
            ResultSlot(
                playerInventory.player,
                this.craftSlots,
                this.resultSlots,
                0,
                124,
                35)
        )

        for (i in 0..2) {
            for (j in 0..2) {
                this.addSlot(Slot(
                    this.craftSlots,
                    j + i * 3,
                    30 + j * 18,
                    17 + i * 18)
                )
            }
        }

        for (k in 0..2) {
            for (i1 in 0..8) {
                this.addSlot(
                    Slot(
                        playerInventory,
                        i1 + k * 9 + 9,
                        8 + i1 * 18,
                        84 + k * 18
                    )
                )
            }
        }

        for (l in 0..8) {
            this.addSlot(Slot(playerInventory, l, 8 + l * 18, 142))
        }
    }

    override fun slotsChanged(inventory: Container) {
        if (!this.placingRecipe) {
            this.access.execute { level: Level, pos: BlockPos? ->
                slotChangedCraftingGrid(
                    this,
                    level,
                    this.player,
                    this.craftSlots,
                    this.resultSlots,
                    null
                )
            }
        }
    }

    public override fun beginPlacingRecipe() {
        this.placingRecipe = true
    }

    public override fun finishPlacingRecipe(recipe: RecipeHolder<CraftingRecipe?>) {
        this.placingRecipe = false
        this.access.execute { level: Level, pos: BlockPos? ->
            slotChangedCraftingGrid(
                this,
                level,
                this.player,
                this.craftSlots,
                this.resultSlots,
                recipe
            )
        }
    }

    override fun fillCraftSlotsStackedContents(itemHelper: StackedContents) {
        this.craftSlots.fillStackedContents(itemHelper)
    }

    override fun clearCraftingContent() {
        this.craftSlots.clearContent()
        this.resultSlots.clearContent()
    }

    override fun recipeMatches(recipe: RecipeHolder<CraftingRecipe?>): Boolean {
        return (recipe.value() as CraftingRecipe).matches(
            this.craftSlots.asCraftInput(),
            this.player.level()
        )
    }

    override fun getResultSlotIndex(): Int  = 0

    override fun getGridWidth(): Int = this.craftSlots.width

    override fun getGridHeight(): Int = this.craftSlots.height

    override fun getSize(): Int = 10

    override fun getRecipeBookType(): RecipeBookType = RecipeBookType.CRAFTING

    override fun removed(player: Player) {
        super.removed(player)
        this.access.execute { level: Level?, pos: BlockPos? ->
            this.clearContainer(
                player,
                this.craftSlots
            )
        }
    }

    override fun stillValid(player: Player): Boolean {
        return stillValid(
            this.access,
            player,
            Blocks.CRAFTING_TABLE
        )
    }

    override fun quickMoveStack(
        player: Player,
        index: Int
    ): ItemStack {
        var stack: ItemStack = ItemStack.EMPTY
        val slot: Slot = this.slots[index]
        if (slot.hasItem()) {
            val stack2: ItemStack = slot.item
            stack = stack2.copy()
            if (index == 0) {
                this.access.execute { level: Level, pos: BlockPos? ->
                    stack2.item.onCraftedBy(stack2, level, player)
                }
                if (!this.moveItemStackTo(stack2, 10, 46, true)) {
                    return ItemStack.EMPTY
                }

                slot.onQuickCraft(stack2, stack)
            } else if (index >= 10 && index < 46) {
                if (!this.moveItemStackTo(stack2, 1, 10, false)) {
                    if (index < 37) {
                        if (!this.moveItemStackTo(stack2, 37, 46, false)) {
                            return ItemStack.EMPTY
                        }
                    } else if (!this.moveItemStackTo(stack2, 10, 37, false)) {
                        return ItemStack.EMPTY
                    }
                }
            } else if (!this.moveItemStackTo(stack2, 10, 46, false)) {
                return ItemStack.EMPTY
            }

            if (stack2.isEmpty) {
                slot.setByPlayer(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }

            if (stack2.count == stack.count) {
                return ItemStack.EMPTY
            }

            slot.onTake(player, stack2)
            if (index == 0) {
                player.drop(stack2, false)
            }
        }

        return stack
    }

    override fun canTakeItemForPickAll(
        stack: ItemStack,
        slot: Slot
    ): Boolean {
        return slot.container !== this.resultSlots && super.canTakeItemForPickAll(stack, slot)
    }

    val resultSlotIndex: Int
        get() = 0

    val gridWidth: Int
        get() = this.craftSlots.width

    val gridHeight: Int
        get() = this.craftSlots.height

    val size: Int
        get() = 10

    val recipeBookType: RecipeBookType
        get() = RecipeBookType.CRAFTING

    override fun shouldMoveToInventory(slotIndex: Int): Boolean {
        return slotIndex != this.resultSlotIndex
    }

    companion object {
        const val RESULT_SLOT: Int = 0
        private const val CRAFT_SLOT_START = 1
        private const val CRAFT_SLOT_END = 10
        private const val INV_SLOT_START = 10
        private const val INV_SLOT_END = 37
        private const val USE_ROW_SLOT_START = 37
        private const val USE_ROW_SLOT_END = 46
        protected fun slotChangedCraftingGrid(
            menu: AbstractContainerMenu,
            level: Level,
            player: Player,
            craftSlots: CraftingContainer,
            resultSlots: ResultContainer,
            recipe: RecipeHolder<CraftingRecipe?>?
        ) {
            if (!level.isClientSide) {
                val craftingInput: CraftingInput = craftSlots.asCraftInput()
                val serverPlayer: ServerPlayer = player as ServerPlayer
                var stack: ItemStack = ItemStack.EMPTY
                val optional =
                    level.server!!.recipeManager
                        .getRecipeFor(
                            RecipeType.CRAFTING,
                            craftingInput,
                            level,
                            recipe
                        )
                if (optional.isPresent) {
                    val recipeHolder: RecipeHolder<CraftingRecipe?> =
                        optional.get()
                    val craftingRecipe: CraftingRecipe =
                        recipeHolder.value() as CraftingRecipe
                    if (resultSlots.setRecipeUsed(level, serverPlayer, recipeHolder)) {
                        val stack2: ItemStack =
                            craftingRecipe.assemble(craftingInput, level.registryAccess())
                        if (stack2.isItemEnabled(level.enabledFeatures())) {
                            stack = stack2
                        }
                    }
                }

                resultSlots.setItem(0, stack)
                menu.setRemoteSlot(0, stack)
                serverPlayer.connection.send(
                    ClientboundContainerSetSlotPacket(
                        menu.containerId,
                        menu.incrementStateId(),
                        0,
                        stack
                    )
                )
            }
        }
    }
}