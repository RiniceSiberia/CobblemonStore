@file:UseSerializers(
    ItemStackSerializer::class,
    BigDecimalSerializer::class
)

package dev.windmill_broken.cobblemon_store.bo.trade

import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.utils.MoneyUtils
import dev.windmill_broken.cobblemon_store.utils.serializer.ItemStackSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.math.BigDecimal
import java.util.*
import kotlin.math.max

@Serializable
sealed class CostObj {

    abstract val type : TradeType

    abstract fun enough(player: ServerPlayer): Boolean

    abstract fun pay(player: ServerPlayer) : Boolean

    abstract fun costMsgComponent() : MutableComponent

    abstract fun costToolTipComponent() : MutableComponent

}

@Serializable
class MoneyCostObj(
    val value: BigDecimal
) : CostObj(){

    override val type: TradeType
        get() = TradeType.MONEY

    override fun enough(player: ServerPlayer): Boolean {
        return run {
            val wallet = MoneyUtils.selectMoney(player)
            wallet >= value
        }
    }

    override fun pay(player: ServerPlayer): Boolean {
        if (player.isCreative) return true
        val origin = MoneyUtils.selectMoney(player)
        val current = MoneyUtils.minusMoney(player,value)
        return origin > current
    }

    override fun costMsgComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.cost.money",String.format("%.2f", value))
    }

    override fun costToolTipComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.slot.cost.money",String.format("%.2f", value))
    }
}

/**
 * 不支持nbt检测
 */
@Serializable
class ItemCostObj(
    val stack: ItemStack,
) : CostObj(){

    override val type: TradeType
        get() = TradeType.ITEM

    override fun enough(player: ServerPlayer): Boolean {
        return player.inventory.items.any {
            it.item == stack.item
        }
    }

    override fun pay(player: ServerPlayer): Boolean {
        if (player.isCreative) return true

        val targets = player.inventory.items.filter {
            it.item == stack.item
        }
        if (targets.sumOf { it.count } < stack.count) {
            player.sendSystemMessage(Component.translatable("msg.cobblemon_store.not_enough_money"))
            return false
        }
        var countVariable = stack.count
        var repeatNum = 0
        while(countVariable>= 0){
            val target = targets.firstOrNull{it.count > 0}?:return false.also {
                player.sendSystemMessage(
                    Component.translatable("msg.cobblemon_store.err", Calendar.getInstance())
                )
                CobblemonStore.Companion.LOGGER.error(
                    "Player ${player.name} attempted to pay $countVariable of ${this.stack.displayName}," +
                            " but only ${targets.sumOf { it.count }} were available in inventory.")
            }
            val shrinkCount = max(countVariable,target.count)
            target.shrink(shrinkCount)
            countVariable -= shrinkCount
            repeatNum++
            if (repeatNum >= 1000){
                Component.translatable("msg.cobblemon_store.err", Calendar.getInstance())
                CobblemonStore.Companion.LOGGER.error("Aborting loop: exceeded maximum allowed iterations (100000). Possible infinite loop.")
            }
        }
        return true
    }

    override fun costMsgComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.cost.item", stack.hoverName,stack.count)
    }

    override fun costToolTipComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.slot.cost.item", stack.hoverName,stack.count)
    }
}