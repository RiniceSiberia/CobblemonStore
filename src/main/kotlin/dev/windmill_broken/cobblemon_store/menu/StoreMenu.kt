package dev.windmill_broken.cobblemon_store.menu

import dev.windmill_broken.cobblemon_store.Registrations
import dev.windmill_broken.cobblemon_store.bo.store.Store
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class StoreMenu(
    containerId : Int,
    val pageIndex : Int,
    val playerInventory : Inventory,
    val store : Store
) : AbstractContainerMenu(
    Registrations.MenuTypes.STORE_MENU.get(),
    containerId
){

    val tradeContainers : Container = SimpleContainer(ROW * COL)

    val player: Player
        get() = playerInventory.player

    val storePageMaxIndex : Int
        get() = (store.trades.size/(PER_PAGE_ITEM_COUNT))


    init {
        val i = (ROW - 4) * 18
        tradeContainers.startOpen(player)
        val trades = store.trades.sortedBy { it.id }
        var itemIndex = pageIndex * PER_PAGE_ITEM_COUNT
        for (r in 0 until ROW) {
            for (c in 0 until COL){
                val slotIndex = c + r * COL
                if (slotIndex in 0 until ROW * COL){
                    val trade = trades.getOrNull(itemIndex)
                    val stack = when(slotIndex){
                        0 -> if (pageIndex > 0){
                            ItemStack(Items.RABBIT_FOOT,pageIndex-1).apply {
                                val name = Component.translatable("menu.cobblemon_store.pre_page", pageIndex)
                                this.set(DataComponents.CUSTOM_NAME, name)
                            }
                        }else{
                            ItemStack(Items.BARRIER).apply {
                                val name = Component.translatable("menu.cobblemon_store.none_pre_page", pageIndex)
                                this.set(DataComponents.CUSTOM_NAME, name)
                            }
                        }
                        8 -> if (pageIndex < storePageMaxIndex){
                            ItemStack(Items.ARROW,pageIndex+1).apply {
                                val name = Component.translatable("menu.cobblemon_store.next_page", pageIndex)
                                this.set(DataComponents.CUSTOM_NAME, name)
                            }
                        }else{
                            ItemStack(Items.BARRIER).also {
                                val name = Component.translatable("menu.cobblemon_store.none_next_page", pageIndex)
                                it.set(DataComponents.CUSTOM_NAME, name)
                            }
                        }
                        else -> {
                            trade?.showedItemStack ?: ItemStack.EMPTY
                        }
                    }

                    tradeContainers.setItem(
                        slotIndex,
                        stack
                    )

                    val onclick :(Player) -> Unit = when(slotIndex){
                        0 ->{
                            if (pageIndex > 0){
                                {player ->
                                    player.openMenu(
                                        StoreMenuProvider(store, pageIndex-1)
                                    )
                                }
                            }else{{}}
                        }
                        8 ->{
                            if(pageIndex < storePageMaxIndex){
                                {player->
                                    player.openMenu(
                                        StoreMenuProvider(store, pageIndex+1)
                                    )
                                }
                            }else {{}}}
                        else -> {
                            if (trade != null){
                                itemIndex++
                                {player ->
                                    trade.trade(player)
                                }
                            } else {{}}
                        }
                    }

                    this.addSlot(
                        StoreSlot(
                            tradeContainers,
                            slotIndex,
                            8 + c * PER_SLOT_OCCUPY,
                            18 + r * PER_SLOT_OCCUPY,
                            onclick
                        )
                    )
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


    override fun quickMoveStack(
        p0: Player,
        p1: Int
    ): ItemStack = ItemStack.EMPTY

    override fun stillValid(p0: Player): Boolean {
        return true
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


        val PER_PAGE_ITEM_COUNT : Int
        get() = (ROW*COL) - PER_PAGE_FUNCTIONAL_BUTTON_COUNT

        val PER_PAGE_FUNCTIONAL_BUTTON_COUNT : Int
            get() = 2 //上一页和下一页
    }
}