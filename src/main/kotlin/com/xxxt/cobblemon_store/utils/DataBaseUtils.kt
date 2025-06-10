package com.xxxt.cobblemon_store.utils

import com.xxxt.cobblemon_store.database.table.StoresTable
import com.xxxt.cobblemon_store.database.table.TradesTable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DataBaseUtils {

    val DB_NAME = Config.db_name


    val jsonConfig = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    val db = lazy {
        Database.connect(
            url = Config.mysql_path + DB_NAME + Config.extra_options,
            driver = Config.mysql_driver,
            user = Config.mysql_user,
            password = Config.mysql_password
        )
    }

    fun register(){
        val iniDB = Database.connect(
            url = Config.mysql_path,
            driver = Config.mysql_driver,
            user = Config.mysql_user,
            password = Config.mysql_password
        )

        transaction(iniDB) {
            SchemaUtils.createDatabase(DB_NAME)
        }

        transaction(db.value) {
            SchemaUtils.create(StoresTable)
            SchemaUtils.create(TradesTable)
            println("Database tables created successfully.")
        }
    }
}