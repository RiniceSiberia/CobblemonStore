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
                if (slotIndex == prePageSlotIndex){
                    this.addSlot(
                        StoreSlot(
                            tradeContainers,
                            slotIndex,
                            8 + c * PER_SLOT_OCCUPY,
                            18 + r * PER_SLOT_OCCUPY
                        ){player ->

                        }
                    )
                }else if (slotIndex == nextPageSlotIndex){
                    this.addSlot(
                        StoreSlot(
                            tradeContainers,
                            slotIndex,
                            8 + c * PER_SLOT_OCCUPY,
                            18 + r * PER_SLOT_OCCUPY
                        ){player ->

                        }
                    )
                }else if (slotIndex in 0 until ROW * COL){
                    val tradeIndex = getTradeIndex(slotIndex)
                    val trade = store.trades.getOrNull(tradeIndex)
                    if (trade != null
                        && slotIndex != prePageSlotIndex
                        && slotIndex != nextPageSlotIndex) {
                        tradeContainers.setItem(
                            tradeIndex,
                            trade.showedItemStack
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


    fun getTradeIndex(slotIndex : Int) : Int{
        var fixedValue = 0
        if (pageIndex * ROW * COL > 0)
            fixedValue -= pageIndex * functionalButtonNum
        if (slotIndex >= prePageSlotIndex) fixedValue--
        if (slotIndex >= nextPageSlotIndex) fixedValue--
        fixedValue += ROW * COL * pageIndex
        return fixedValue + slotIndex
    }

    val prePageSlotIndex : Int
        get(){
            return if (pageIndex !in 1 until store.trades.size){
                -1
            }else{
                COL * (ROW - 1)
            }
        }

    val nextPageSlotIndex : Int
        get(){
            return if (pageIndex !in 0 until store.trades.size-1){
                -1
            }else{
                COL * ROW - 1
            }
        }

    val functionalButtonNum : Int
        get(){
            return listOf(
                ::prePageSlotIndex,
                ::nextPageSlotIndex
            ).count{ it() >= 0 }
        }

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