package dev.windmill_broken.cobblemon_store.menu

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class StoreSlot(
    container : Container,
    slot : Int,
    x : Int,
    y : Int,
    val onclick : (Player) -> Unit = { },
) : Slot(
    container,
    slot,
    x,
    y
){
    override fun mayPickup(player: Player): Boolean {
        if (player is ServerPlayer){
            onclick(player)
        }
        return false
    }

    override fun mayPlace(stack: ItemStack): Boolean = false

    override fun onTake(player: Player, stack: ItemStack) {}

}