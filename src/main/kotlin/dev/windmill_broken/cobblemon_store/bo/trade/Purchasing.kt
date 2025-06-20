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
import dev.windmill_broken.cobblemon_store.utils.MoneyUtils
import dev.windmill_broken.cobblemon_store.utils.serializer.ItemStackSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.PokemonSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.BigDecimalSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonClassDiscriminator
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.math.BigDecimal

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("purchasing_type")
sealed interface Purchasing{

    fun purchasing(player : Player,superTrade : Trade) : WarehouseItem

    fun purchasingMsgComponent() : MutableComponent

    fun purchasingTooltipComponent() : MutableComponent

}

@Serializable
@SerialName("ITEM_STACK_PURCHASING")
class ItemStackPurchasing(
        val stack : ItemStack
    ) : Purchasing{

        constructor(
            item : Item,
            count : Int = 1
        ) : this(
            ItemStack(item,count)
        )

        override fun purchasing(player: Player, superTrade: Trade): WarehouseItem {
            return ItemWarehouseItem(
                superTrade.creator,
                stack
            )
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
@SerialName("MONEY_PURCHASING")
class MoneyPurchasing(
    val value : BigDecimal,
    @SerialName("currency_type")
    val currencyType : String = MoneyUtils.primaryCurrency.key().value()
) : Purchasing{

    constructor(value : Int) : this(value.toBigDecimal())

    constructor(value : Double) : this(value.toBigDecimal())

    constructor(value : String) : this(value.toBigDecimal())


    override fun purchasing(player: Player, superTrade: Trade): WarehouseItem {
        return MoneyWarehouseItem(
            superTrade.creator,
            value,
            currencyType
        )
    }

    override fun purchasingMsgComponent(): MutableComponent {
        return Component.translatable("msg.cobblemon_store.purchasing.money", String.format("%.2f", value))
    }

    override fun purchasingTooltipComponent(): MutableComponent {
        return Component.translatable(
            "msg.cobblemon_store.slot.purchasing.money",
            String.format("%.2f", value)
        )
    }

}
@Serializable
@SerialName("POKEMON_PURCHASING")
class PokemonPurchasing(
    val pokemon : Pokemon
) : Purchasing{

    override fun purchasing(player: Player, superTrade: Trade): WarehouseItem {
        val warehouse = DAOWharf.warehouseLibrary.getOrCreate(player.uuid)
        return PokemonWarehouseItem(
            superTrade.creator,
            pokemon
        )
    }

    override fun purchasingMsgComponent(): MutableComponent {
        return Component.translatable(
            "msg.cobblemon_store.purchasing.pokemon",
            pokemon.getDisplayName()
        )
    }

    override fun purchasingTooltipComponent(): MutableComponent {
        return Component.translatable(
            "msg.cobblemon_store.slot.purchasing.pokemon",
            pokemon.getDisplayName()
        )
    }

}