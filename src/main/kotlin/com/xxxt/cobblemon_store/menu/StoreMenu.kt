package com.xxxt.cobblemon_store.menu

import com.xxxt.cobblemon_store.Registrations.MenuTypes.STORE_MENU
import com.xxxt.cobblemon_store.Registrations.StoreBlocks.STORE_BLOCK
import com.xxxt.cobblemon_store.store.Store
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.levelgen.SurfaceRules.state

class StoreMenu(
    containerId : Int,
    val pageIndex : Int,
    val playerInventory : Inventory,
    val store : Store
) : AbstractContainerMenu(
    STORE_MENU.get(),
    containerId
){

    val tradeContainers : Container = SimpleContainer(ROW * COL)

    val player: Player
        get() = playerInventory.player

    val access : ContainerLevelAccess =
        ContainerLevelAccess.create(
            player.level(),
            player.blockPosition()
        )

    init {
        val i = (ROW - 4) * 18
        tradeContainers.startOpen(player)
        for (r in 0 until ROW) {
            for (c in 0 until COL){
                val slotIndex = c + r * COL
                if (slotIndex in 0 until ROW * COL){
                    val tradeIndex = getTradeIndex(slotIndex)
                    val trade = store.trades.getOrNull(tradeIndex)
                    if (trade != null) {
                        tradeContainers.setItem(
                            tradeIndex,
                            trade.showedItemStack
                        )
                        this.addSlot(
                            StoreSlot(
                                tradeContainers,
                                slotIndex,
                                8 + c * PER_SLOT_OCCUPY,
                                18 + r * PER_SLOT_OCCUPY,
                            ){player ->
                                trade.trade(player)
                            }
                        )
                    }
                }
            }
        }

        for (r in 0 until 3){
            for (c in 0 until 9){
                this.addSlot(
                    Slot(
                        playerInventory,
                        c + r * 9,
                        8 + c * PER_SLOT_OCCUPY,
                        103 + r * PER_SLOT_OCCUPY + i
                    )
                )
            }
        }

        for(r in 0 until 9){
            this.addSlot(
                Slot(
                    playerInventory,
                    r,
                    8 + r * PER_SLOT_OCCUPY,
                    161 + i
                )
            )
        }
    }


    fun getTradeIndex(slotIndex : Int) : Int = pageIndex * ROW * COL + slotIndex


    override fun quickMoveStack(
        p0: Player,
        p1: Int
    ): ItemStack = ItemStack.EMPTY

    override fun stillValid(p0: Player): Boolean {
        return stillValid(
            access,
            player,
            STORE_BLOCK.get()
        )
    }

    override fun removed(player: Player) {
        super.removed(player)
        tradeContainers.stopOpen(player)
    }


    companion object{
        val ROW : Int
            get() = 6

        val COL : Int
            get() = 9

        val PER_SLOT_SIZE : Int
            get() = 16

        val PER_SLOT_MARGIN : Int
            get() = 1

        val PER_SLOT_OCCUPY : Int
            get() = PER_SLOT_SIZE + 2 * PER_SLOT_MARGIN
    }
}