@file:UseSerializers(
    UUIDSerializer::class,
    ItemStackSerializer::class,
    PokemonSerializer::class,
    BigDecimalSerializer::class
)
package dev.windmill_broken.cobblemon_store.bo.warehouse

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.removeIf
import dev.windmill_broken.cobblemon_store.bo.trade.TradeCreator
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import dev.windmill_broken.cobblemon_store.utils.MoneyUtils
import dev.windmill_broken.cobblemon_store.utils.serializer.BigDecimalSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.ItemStackSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.PokemonSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.UUIDSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonClassDiscriminator
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.ItemHandlerHelper
import java.math.BigDecimal


/**
 * 仓库存储的单个项
 */


@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("warehouse_item_type")
sealed class WarehouseItem{

    abstract val creator : TradeCreator

    /**
     * 成交/取回
     */
    abstract fun retrieve(superWarehouse : Warehouse)
    /**
     * 删除这个物品,并保存到数据库/json里
     */
    fun removeIt(superWarehouse : Warehouse){
        superWarehouse.removeIf { it ->
                it.value == this
            }
        DAOWharf.warehouseLibrary.update(superWarehouse.playerUUID,superWarehouse)
    }
}

@Serializable
@SerialName("MONEY_WAREHOUSE_ITEM")
class MoneyWarehouseItem(
    override val creator: TradeCreator,
    val value : BigDecimal,
    val type : String
): WarehouseItem(){

    override fun retrieve(superWarehouse: Warehouse) {
        MoneyUtils.addMoney(superWarehouse.player, value,type)
        removeIt(superWarehouse)
    }
}

@Serializable
@SerialName("ITEM_STACK_WAREHOUSE_ITEM")
class ItemWarehouseItem(
    override val creator: TradeCreator,
    val stack : ItemStack
): WarehouseItem(){

    override fun retrieve(superWarehouse: Warehouse) {
        ItemHandlerHelper.giveItemToPlayer(superWarehouse.player,stack)
        removeIt(superWarehouse)
    }
}


@Serializable
@SerialName("POKEMON_WAREHOUSE_ITEM")
class PokemonWarehouseItem(
    override val creator: TradeCreator,
    val pokemon: Pokemon
) : WarehouseItem(){

    override fun retrieve(superWarehouse: Warehouse) {
        superWarehouse.player.party().add(pokemon)
        removeIt(superWarehouse)
    }
}