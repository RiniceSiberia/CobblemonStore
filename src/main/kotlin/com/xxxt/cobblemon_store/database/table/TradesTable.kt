package com.xxxt.cobblemon_store.database.table

import com.xxxt.cobblemon_store.store.Trade
import com.xxxt.cobblemon_store.utils.DataBaseUtils
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.json.json

object TradesTable : IntIdTable("trades") {
    val index = integer("index")
    val content = json(
        "content",
        DataBaseUtils.jsonConfig,
        Trade.serializer())
    val store = reference("id", StoresTable.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

}