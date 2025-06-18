@file:UseSerializers(
    UUIDSerializer::class,
    ItemStackSerializer::class,
    PokemonSerializer::class
)
package dev.windmill_broken.cobblemon_store.bo.warehouse

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.party
import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.bo.trade.TradeType
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import dev.windmill_broken.cobblemon_store.utils.PluginUtils
import dev.windmill_broken.cobblemon_store.utils.serializer.ItemStackSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.PokemonSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.ItemHandlerHelper
import java.util.*


/**
 * 仓库存储的单个项
 */

@Serializable
sealed class WarehouseItem{
    abstract val playerUUID: UUID
    abstract val index : Int
    val player get() = CobblemonStore.Companion.server.playerList.getPlayer(playerUUID)!!

    abstract val type : TradeType
    /**
     * 成交/取回
     */
    abstract fun retrieve()
    /**
     * 删除这个物品,并保存到数据库/json里
     */
    fun removeIt(){
        DAOWharf.warehouseLibrary.getOrCreate(playerUUID).apply {
            this.remove(index)
            DAOWharf.warehouseLibrary.update(playerUUID,this)
        }
    }
}

@Serializable
class MoneyWarehouseItem(
    override val playerUUID: UUID,
    override val index : Int,
    val price : Double
): WarehouseItem(){
    override val type: TradeType
        get() = TradeType.MONEY

    override fun retrieve() {
        PluginUtils.addMoney(player, price)
        removeIt()
    }
}

@Serializable
class ItemWarehouseItem(
    override val playerUUID: UUID,
    override val index : Int,
    val stack : ItemStack
): WarehouseItem(){
    override val type: TradeType
        get() = TradeType.ITEM

    override fun retrieve() {
        ItemHandlerHelper.giveItemToPlayer(player,stack)
    }
}


@Serializable
class PokemonWarehouseItem(
    override val playerUUID: UUID,
    override val index : Int,
    val pokemon: Pokemon
) : WarehouseItem(){
    override val type: TradeType
        get() = TradeType.POKEMON

    override fun retrieve() {
        player.party().add(pokemon)
        removeIt()
    }
}