@file:UseSerializers(
    UUIDSerializer::class,
    CalendarSerializer::class
)
package dev.windmill_broken.cobblemon_store.bo.trade

import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import dev.windmill_broken.cobblemon_store.utils.TimeUtils
import dev.windmill_broken.cobblemon_store.utils.serializer.CalendarSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player
import java.util.*


/**
 * 实现该接口为有不同程度上的存货上限
 */
@Serializable
sealed interface StoreLimit{

    val superTradeId : Int

    val superTrade
        get() = DAOWharf.tradeLibrary.get(superTradeId)

    val type : String

    fun couldBuy(player: Player) : Boolean

    fun consume(player: Player)

    fun restock()

    fun getTooltipComponent(player: Player) : MutableComponent
}

@Serializable
class GlobalStoreLimit(
    override val superTradeId: Int,
    val limit : Int,
    var sales : Int = 0,
//    val identity : Set<StoreIdentity> = emptySet()
) : StoreLimit{

    override val type: String
        get() = "global"

    override fun couldBuy(player: Player): Boolean {
        return sales < limit
    }

    override fun consume(player: Player) {
        sales++
        superTrade?.saveChange()
    }

    override fun restock() {
        sales = 0
        superTrade?.saveChange()
    }

    override fun getTooltipComponent(player: Player): MutableComponent {
        return Component.translatable(
            "item.cobblemon_store.sell_menu.slot.reserve.global",
            limit-sales,
            limit
            ).apply {
                if (sales >= limit){
                    withStyle(ChatFormatting.RED)
                }
        }
    }

}

@Serializable
open class PollLimit(
    override val superTradeId: Int,
    open val limit : Int,
    val playerBought : MutableMap<UUID, Int> = mutableMapOf()
) : StoreLimit{

    override val type: String
        get() = "poll_limit"

    override fun couldBuy(player: Player): Boolean {
        return playerBought.getOrDefault(player.uuid, 0) < limit
    }

    override fun consume(player: Player) {
        playerBought[player.uuid] = playerBought.getOrDefault(player.uuid, 0) + 1
        superTrade?.saveChange()
    }

    override fun restock() {
        playerBought.clear()
        superTrade?.saveChange()
    }

    override fun getTooltipComponent(player: Player): MutableComponent {
        return Component.translatable(
            "item.cobblemon_store.sell_menu.slot.reserve.poll_limit",
            (limit - playerBought.getOrDefault(player.uuid, 0)),
            limit
        ).apply {
            if (limit <= playerBought.getOrDefault(player.uuid, 0)){
                withStyle(ChatFormatting.RED)
            }
        }
    }
}

@Serializable
class TimeLimitStoreLimit(
    override val superTradeId: Int,
    var timeStamp : Long = let {
        val current = Calendar.getInstance()
        current.add(Calendar.DATE,1)
        current.timeInMillis
    }
) : StoreLimit{

    val expirationTime : Calendar
        get() = Calendar.getInstance().apply { timeInMillis = timeStamp }

    val isExpired
        get() = timeStamp <= Calendar.getInstance().timeInMillis

    override val type: String
        get() = "time_limit"

    override fun couldBuy(player: Player): Boolean {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")) <= expirationTime
    }

    override fun consume(player: Player) {
        superTrade?.saveChange()
    }

    override fun restock() {
        val tomorrow = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        tomorrow.add(Calendar.HOUR, 1)
        timeStamp = tomorrow.timeInMillis
        superTrade?.saveChange()
    }

    override fun getTooltipComponent(player: Player): MutableComponent {
        return TimeUtils.getExpiredText(
            expirationTime,
            "item.cobblemon_store.sell_menu.slot.reserve.time_limit"
        )
    }

}