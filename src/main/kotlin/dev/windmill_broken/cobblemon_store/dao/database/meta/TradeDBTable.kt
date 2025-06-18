package dev.windmill_broken.cobblemon_store.dao.database.meta

import dev.windmill_broken.cobblemon_store.bo.trade.CostObj
import dev.windmill_broken.cobblemon_store.bo.trade.PurchasingObj
import dev.windmill_broken.cobblemon_store.bo.trade.StoreLimit
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.kJsonConfig
import kotlinx.serialization.builtins.MapSerializer
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
    val cost = jsonb(
        name = "cost",
        jsonConfig = kJsonConfig,
        kSerializer = CostObj.serializer()
    )
    val purchasing = jsonb(
        name = "purchasing",
        jsonConfig = kJsonConfig,
        kSerializer = PurchasingObj.serializer()
    )
    val storeLimits = jsonb(
        name = "store_limits",
        jsonConfig = kJsonConfig,
        kSerializer = MapSerializer(
            String.serializer(),
            StoreLimit.serializer()
        )
    )

}