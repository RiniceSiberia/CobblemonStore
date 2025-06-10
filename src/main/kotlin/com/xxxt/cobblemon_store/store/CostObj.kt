@file:UseSerializers(
    ResourceLocationSerializer::class,
    ItemStackSerializer::class
)

package com.xxxt.cobblemon_store.store

import com.mojang.logging.LogUtils
import com.xxxt.cobblemon_store.utils.ItemStackSerializer
import com.xxxt.cobblemon_store.utils.PluginUtils
import com.xxxt.cobblemon_store.utils.ResourceLocationSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.util.*
import kotlin.math.max
import kotlin.reflect.KClass

@Serializable
sealed class CostObj<C : Comparable<C>> {
    abstract val clazz : KClass<C>

    abstract fun enough(player: Player): Boolean

    abstract fun pay(player: Player) : Boolean

    abstract fun costComponent() : MutableComponent
}

@Serializable
class GTSMoneyCostObj(
    val price : Double
) : CostObj<Double>(){

    override val clazz: KClass<Double>
        get() = Double::class

    override fun enough(player: Player): Boolean {
        return if (player is ServerPlayer && PluginUtils.checkBukkitInstalled()){
            val wallet = PluginUtils.getValut(player)?:return false
            wallet >= price
        }else{
            false
        }
    }

    override fun pay(player: Player): Boolean {
        if (player.isCreative) return true
        return PluginUtils.minusMoney(player,price)
    }

    override fun costComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.cost.money",String.format("%.2f", price))
    }
}

/**
 * 不支持nbt检测
 */
@Serializable
class ItemCostObj(
    val itemStack: ItemStack,
) : CostObj<Int>(){

    override val clazz: KClass<Int>
        get() = Int::class

    override fun enough(player: Player): Boolean {
        return player.inventory.items.any {
            it.item == itemStack.item
        }
    }

    override fun pay(player: Player): Boolean {
        if (player.isCreative) return true

        val targets = player.inventory.items.filter {
            it.item == itemStack.item
        }
        if (targets.sumOf { it.count } < itemStack.count) {
            player.sendSystemMessage(Component.translatable("msg.cobblemon_store.not_enough_money"))
            return false
        }
        var countVariable = itemStack.count
        var repeatNum = 0
        while(countVariable>= 0){
            val target = targets.firstOrNull{it.count > 0}?:return false.also {
                player.sendSystemMessage(
                    Component.translatable("cobblemon_store_err", Calendar.getInstance())
                )
                LOGGER.error(
                    "Player ${player.name} attempted to pay $countVariable of ${this.itemStack.displayName}," +
                            " but only ${targets.sumOf { it.count }} were available in inventory.")
            }
            val shrinkCount = max(countVariable,target.count)
            target.shrink(shrinkCount)
            countVariable -= shrinkCount
            repeatNum++
            if (repeatNum >= 1000){
                Component.translatable("cobblemon_store_err", Calendar.getInstance())
                LOGGER.error("Aborting loop: exceeded maximum allowed iterations (100000). Possible infinite loop.")
            }
        }
        return true
    }

    override fun costComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.cost.item", itemStack.hoverName,itemStack.count)
    }
}

private val LOGGER: org.slf4j.Logger = LogUtils.getLogger()