@file:UseSerializers(
    ItemStackSerializer::class,
    BigDecimalSerializer::class,
    ResourceLocationSerializer::class
)

package dev.windmill_broken.cobblemon_store.bo.trade

import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.utils.MoneyUtils
import dev.windmill_broken.cobblemon_store.utils.serializer.BigDecimalSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.ItemStackSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.ResourceLocationSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonClassDiscriminator
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("cost_type")
sealed interface Cost {

    fun enough(player: ServerPlayer): Boolean

    fun pay(player: ServerPlayer): Boolean

    fun costMsgComponent(): MutableComponent

    fun costToolTipComponent(): MutableComponent

}

@Serializable
@SerialName("FREE_COST")
data object FreeCost : Cost {
    override fun enough(player: ServerPlayer): Boolean {
        return true
    }

    override fun pay(player: ServerPlayer): Boolean {
        return true
    }

    override fun costMsgComponent(): MutableComponent {
        return Component.empty()
    }

    override fun costToolTipComponent(): MutableComponent {
        return Component.empty()
    }
}

@Serializable
@SerialName("MONEY_COST")
class MoneyCost(
    val value: BigDecimal,
    @SerialName("currency_type")
    val currencyType: String = MoneyUtils.primaryCurrency.key().value()
) : Cost {

    constructor(
        value: Int
    ) : this(value.toBigDecimal())

    constructor(
        value: Double
    ) : this(value.toBigDecimal())

    constructor(
        value: String
    ) : this(value.toBigDecimal())


    override fun enough(player: ServerPlayer): Boolean {
        val wallet = MoneyUtils.getCurrency(player, currencyType)
        return wallet >= value
    }

    override fun pay(player: ServerPlayer): Boolean {
//        if (player.isCreative) return true
        val origin = MoneyUtils.getCurrency(player, currencyType)
        val current = MoneyUtils.minusMoney(player, value, currencyType)
        return origin > current
    }

    override fun costMsgComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.cost.money", String.format("%.2f", value))
    }

    override fun costToolTipComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.slot.cost.money", String.format("%.2f", value))
    }
}

/**
 * 不支持nbt检测
 */
@Serializable
@SerialName("SIMPLE_ITEM_COST")
class SimpleItemCost(
    val item: ResourceLocation,
    val count: Int = 1
) : Cost {

    val tempStack: ItemStack?
        get() {
            val got = BuiltInRegistries.ITEM.getOptional(item)
            return if (got.isEmpty) {
                null
            } else {
                ItemStack(got.get(), count)
            }
        }

    override fun enough(player: ServerPlayer): Boolean {
        val count = player.inventory.items.sumOf {
            val registerKey = BuiltInRegistries.ITEM.getKey(it.item)
            if (registerKey == item) it.count else 0
        }
        return count >= this.count
    }

    override fun pay(player: ServerPlayer): Boolean {
//        if (player.isCreative) return true

        val targets = player.inventory.items.filter {
            val registerKey = BuiltInRegistries.ITEM.getKey(it.item)
            registerKey == item
        }

        if (targets.sumOf { it.count } < count) {
            player.sendSystemMessage(Component.translatable("msg.cobblemon_store.not_enough_money"))
            return false
        }
        var countVariable = count
        var repeatNum = 0
        while (countVariable > 0) {
            val target = targets.firstOrNull { it.count > 0 } ?: return false.also {
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                player.sendSystemMessage(
                    Component.translatable("msg.cobblemon_store.err", formatter.format(Calendar.getInstance().time))
                )
                CobblemonStore.Companion.LOGGER.error(
                    "Player ${player.name} attempted to pay $countVariable of ${this.item}," +
                            " but only ${targets.sumOf { it.count }} were available in inventory.")
            }
            val shrinkCount = min(countVariable, target.count)
            target.shrink(shrinkCount)
            countVariable -= shrinkCount
            repeatNum++
            if (repeatNum >= 1000) {
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                Component.translatable("msg.cobblemon_store.err", formatter.format(Calendar.getInstance().time))
                CobblemonStore.Companion.LOGGER.error("Aborting loop: exceeded maximum allowed iterations (1000). Possible infinite loop.")
            }
        }
        return true
    }

    override fun costMsgComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.cost.item", tempStack?.hoverName ?: "", count)
    }

    override fun costToolTipComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.slot.cost.item", tempStack?.hoverName ?: "", count)
    }
}