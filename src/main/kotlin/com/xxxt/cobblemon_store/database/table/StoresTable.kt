package com.xxxt.cobblemon_store.database.table

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object StoresTable : IntIdTable("stores"){
    val name = text("name").uniqueIndex()
    val description = text("description").nullable()
}