@file:UseSerializers(
    ItemStackSerializer::class,
    PokemonSerializer::class,
    BigDecimalSerializer::class
)
package dev.windmill_broken.cobblemon_store.bo.trade

import com.cobblemon.mod.common.pokemon.Pokemon
import dev.windmill_broken.cobblemon_store.bo.warehouse.ItemWarehouseItem
import dev.windmill_broken.cobblemon_store.bo.warehouse.MoneyWarehouseItem
import dev.windmill_broken.cobblemon_store.bo.warehouse.PokemonWarehouseItem
import dev.windmill_broken.cobblemon_store.bo.warehouse.WarehouseItem
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import dev.windmill_broken.cobblemon_store.utils.serializer.ItemStackSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.PokemonSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.math.BigDecimal

@Serializable
sealed class PurchasingObj{

    val successMsgPath = "msg.cobblemon_store.purchasing.${type.lowercaseName}"

    val tooltipMsgPath = "msg.cobblemon_store.slot.purchasing.${type.lowercaseName}"

    abstract val type : TradeType

    abstract fun purchasing(player : Player) : WarehouseItem

    abstract fun purchasingMsgComponent() : MutableComponent

    abstract fun purchasingTooltipComponent() : MutableComponent

}

@Serializable
class ItemPurchasingObj(
        val stack : ItemStack
    ) : PurchasingObj(){

        override val type: TradeType
            get() = TradeType.ITEM

        override fun purchasing(player: Player): ItemWarehouseItem {
            val warehouse = DAOWharf.warehouseLibrary.getOrCreate(player.uuid)
            return ItemWarehouseItem(
                player.uuid,
                warehouse.nextEmptyIndex,
                stack
            )
        }

        override fun purchasingMsgComponent(): MutableComponent {
            return Component.translatable(successMsgPath,stack.hoverName,stack.count)
        }

        override fun purchasingTooltipComponent(): MutableComponent {
            return Component.translatable(
                tooltipMsgPath,
                stack.displayName,
                stack.count
            )
        }
    }

@Serializable
class MoneyPurchasingObj(
    val value : BigDecimal
) : PurchasingObj(){

    override val type: TradeType
        get() = TradeType.MONEY

    override fun purchasing(player: Player): MoneyWarehouseItem {
        val warehouse = DAOWharf.warehouseLibrary.getOrCreate(player.uuid)
        return MoneyWarehouseItem(
            player.uuid,
            warehouse.nextEmptyIndex,
            value
        )
    }

    override fun purchasingMsgComponent(): MutableComponent {
        return Component.translatable(successMsgPath, String.format("%.2f", value))
    }

    override fun purchasingTooltipComponent(): MutableComponent {
        return Component.translatable(
            tooltipMsgPath,
            String.format("%.2f", value)
        )
    }

}
@Serializable
class PokemonPurchasingObj(
    val pokemon : Pokemon
) : PurchasingObj(){

    override val type: TradeType
        get() = TradeType.POKEMON

    override fun purchasing(player: Player): PokemonWarehouseItem {
        val warehouse = DAOWharf.warehouseLibrary.getOrCreate(player.uuid)
        return PokemonWarehouseItem(
            player.uuid,
            warehouse.nextEmptyIndex,
            pokemon
        )
    }

    override fun purchasingMsgComponent(): MutableComponent {
        return Component.translatable(
            successMsgPath,
            pokemon.getDisplayName()
        )
    }

    override fun purchasingTooltipComponent(): MutableComponent {
        return Component.translatable(
            tooltipMsgPath,
            pokemon.getDisplayName()
        )
    }

}