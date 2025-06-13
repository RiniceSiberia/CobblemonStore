@file:UseSerializers(
    ItemStackSerializer::class
)
package com.xxxt.cobblemon_store.store

import com.xxxt.cobblemon_store.utils.ItemStackSerializer
import com.xxxt.cobblemon_store.utils.PluginUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.ItemHandlerHelper
import kotlin.reflect.KClass

@Serializable
sealed class PurchasingObj<T: Any>{

    abstract val clazz : KClass<out T>

    abstract fun purchasing(player : Player)

    abstract fun purchasingMsgComponent() : MutableComponent

    abstract fun purchasingTooltipComponent() : MutableComponent
}

@Serializable
open class ItemPurchasingObj(
    open val stack : ItemStack
) : PurchasingObj<ItemStack>(){
    override val clazz: KClass<out ItemStack>
        get() = stack::class

    override fun purchasing(player: Player) {
        ItemHandlerHelper.giveItemToPlayer(player,stack)
    }

    override fun purchasingMsgComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.purchasing.item",stack.hoverName,stack.count)
    }

    override fun purchasingTooltipComponent(): MutableComponent {
        return Component.translatable(
            "msg.cobblemon_store.slot.purchasing.item",
            stack.displayName,
            stack.count
        )
    }
}

@Serializable
class GTSMoney(
    val value : Double
) : PurchasingObj<Double>(){
    override val clazz: KClass<out Double>
        get() = Double::class

    override fun purchasing(player: Player) {
        PluginUtils.addMoney(player, value)
    }

    override fun purchasingMsgComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.purchasing.gts_money", String.format("%.2f", value))
    }

    override fun purchasingTooltipComponent(): MutableComponent {
        return Component.translatable(
            "msg.cobblemon_store.slot.purchasing.gts_money",
            String.format("%.2f", value)
        )
    }
}