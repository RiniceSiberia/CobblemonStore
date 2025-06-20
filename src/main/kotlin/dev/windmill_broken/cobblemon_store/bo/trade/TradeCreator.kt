@file:UseSerializers(
    UUIDSerializer::class
)
package dev.windmill_broken.cobblemon_store.bo.trade

import dev.windmill_broken.cobblemon_store.utils.serializer.CalendarSerializer
import dev.windmill_broken.cobblemon_store.utils.serializer.UUIDSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonClassDiscriminator
import net.minecraft.world.entity.player.Player
import java.util.UUID

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("creator_type")
sealed interface TradeCreator


@Serializable
@SerialName("SERVER_CREATOR")
data object ServerTradeCreator : TradeCreator

@Serializable
@SerialName("PLAYER_CREATOR")
data class PlayerTradeCreator(
    @SerialName("player_id")
    val playerId : UUID
) : TradeCreator{
    constructor(player : Player) : this(player.uuid)
}