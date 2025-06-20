package dev.windmill_broken.cobblemon_store.dao.database.meta

import dev.windmill_broken.cobblemon_store.bo.trade.Cost
import dev.windmill_broken.cobblemon_store.bo.trade.Purchasing
import dev.windmill_broken.cobblemon_store.bo.trade.StoreLimit
import dev.windmill_broken.cobblemon_store.bo.trade.TradeCreator
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.kJsonConfig
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.json.jsonb

object TradeDBTable : IntIdTable("trade","t_id") {
    val storeId = reference(
        "store_id",
        StoreDBTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE,
    )
    val creator = jsonb(
        name = "creator",
        jsonConfig = kJsonConfig,
        kSerializer = TradeCreator.serializer()
    )
    val cost = jsonb(
        name = "cost",
        jsonConfig = kJsonConfig,
        kSerializer = Cost.serializer()
    )
    val purchasing = jsonb(
        name = "purchasing",
        jsonConfig = kJsonConfig,
        kSerializer = Purchasing.serializer()
    )
    val storeLimits = jsonb(
        name = "store_limits",
        jsonConfig = kJsonConfig,
        kSerializer = SetSerializer(
            StoreLimit.serializer()
        )
    )

}