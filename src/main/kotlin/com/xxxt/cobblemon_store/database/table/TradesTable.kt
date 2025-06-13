package com.xxxt.cobblemon_store.database.table

import com.xxxt.cobblemon_store.store.Trade
import com.xxxt.cobblemon_store.utils.JsonFileUtils.jsonConfig
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.json.json

object TradesTable : IntIdTable("trades") {
    val content = json(
        "content",
        jsonConfig,
        Trade.serializer())
    val store = reference("id", StoresTable.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

}