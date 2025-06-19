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
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.math.BigDecimal

@Serializable
sealed class Purchasing{

    val successMsgPath = "msg.cobblemon_store.purchasing.${sellType.lowercaseName}"

    val tooltipMsgPath = "msg.cobblemon_store.slot.purchasing.${sellType.lowercaseName}"

    abstract val sellType : SellType

    abstract fun purchasing(player : Player) : WarehouseItem

    abstract fun purchasingMsgComponent() : MutableComponent

    abstract fun purchasingTooltipComponent() : MutableComponent

}

@Serializable
class ItemPurchasing(
        val stack : ItemStack
    ) : Purchasing(){

        constructor(
            item : Item,
            count : Int = 1
        ) : this(
            ItemStack(item,count)
        )

        override val sellType: SellType
            get() = SellType.ITEM

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
class MoneyPurchasing(
    val value : BigDecimal,
    val type : String = MoneyUtils.primaryCurrency.key().value()
) : Purchasing(){

    constructor(value : Int) : this(value.toBigDecimal())

    constructor(value : Double) : this(value.toBigDecimal())

    constructor(value : String) : this(value.toBigDecimal())

    override val sellType: SellType
        get() = SellType.MONEY

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
class PokemonPurchasing(
    val pokemon : Pokemon
) : Purchasing(){

    override val sellType: SellType
        get() = SellType.POKEMON

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