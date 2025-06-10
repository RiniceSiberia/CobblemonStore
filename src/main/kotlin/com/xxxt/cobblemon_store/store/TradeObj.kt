@file:UseSerializers(
    UUIDSerializer::class,
    ItemStackSerializer::class
)
package com.xxxt.cobblemon_store.store

import com.xxxt.cobblemon_store.utils.ItemStackSerializer
import com.xxxt.cobblemon_store.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import java.util.*

@Serializable
class Trade(
    val index : Int,
    val cost : CostObj<*>?,
    val purchasing : PurchasingObj<*>,
    val reserve : Reserve?
){

    fun trade( player: Player) : Boolean{
        if (cost != null && !cost.enough(player)){
            player.sendSystemMessage(Component.translatable("msg.cobblemon_store.not_enough_money"))
            return false
        }
        if (reserve != null && !reserve.couldBuy(player)){
            player.sendSystemMessage(Component.translatable("msg.cobblemon_store.sold_out"))
            return false
        }
        cost?.pay(player)?.also { if(!it) return false }
        reserve?.consume(player)
        purchasing.purchasing(player)
        player.sendSystemMessage(
            purchasing.payComponent().also {
                if (cost!=null)
                    it.append(cost.costComponent())
            }
        )
        return true
    }



}


/**
 * 实现该接口为有不同程度上的存货上限
 */
@Serializable
sealed interface Reserve{
    fun couldBuy(player: Player) : Boolean

    fun consume(player: Player)

    fun restock()
}

@Serializable
class GlobalReserve(
    val limit : Int,
    var sales : Int = 0,
//    val identity : Set<StoreIdentity> = emptySet()
) : Reserve{
    override fun couldBuy(player: Player): Boolean {
        return sales < limit
    }

    override fun consume(player: Player) {
        sales++
    }

    override fun restock() {
        sales = 0
    }

}

@Serializable
open class SimpleSingleReserve(
    open val limit : Int,
    val playerBought : MutableMap<UUID, Int> = mutableMapOf()
) : Reserve{
    override fun couldBuy(player: Player): Boolean {
        return playerBought.getOrDefault(player.uuid, 0) < limit
    }

    override fun consume(player: Player) {
        playerBought[player.uuid] = playerBought.getOrDefault(player.uuid, 0) + 1
    }

    override fun restock() {
        playerBought.clear()
    }
}