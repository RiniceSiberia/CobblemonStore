package com.xxxt.cobblemon_store.utils

import com.xxxt.cobblemon_store.database.dao.StoreEntity
import com.xxxt.cobblemon_store.database.table.StoresTable
import com.xxxt.cobblemon_store.database.table.TradesTable
import com.xxxt.cobblemon_store.store.StoresLibrary
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.collections.putAll

object DataBaseUtils {

    val DB_NAME: String
        get() = Config.db_name


    val db = lazy {
        Database.connect(
            url = Config.mysql_path + DB_NAME + Config.extra_options,
            driver = Config.mysql_driver,
            user = Config.mysql_user,
            password = Config.mysql_password
        )
    }

    init{
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

    fun loadByDataBase() : Boolean{
        return try {
            transaction(db.value) {
                StoreEntity.all().map {
                    it.id.value to it.toBO()
                }
            }.also {
                StoresLibrary.putAll(it)
            }
            true
        }catch (e: Exception){
            LOGGER.error("database could not launch,system automatically switch to json file load mode", e)
            false
        }
    }

    fun saveByDataBase() : Boolean{
        return try {
            transaction(db.value) {
                StoresTable.batchUpsert(
                    data = StoresLibrary.values,
                    onUpdateExclude = listOf(StoresTable.id)
                ) { store ->
                    this[StoresTable.name] = store.name
                    this[StoresTable.description] = store.description
                }

                TradesTable.batchUpsert(
                    data = StoresLibrary.values.map {
                        it.id to it.trades
                    },
                    onUpdateExclude = listOf(TradesTable.id)
                ){ (storeId,trades) ->
                    trades.forEach { t ->
                        this[TradesTable.store] = storeId
                        this[TradesTable.content] = t
                    }
                }
            }
            true
        }catch (e : Exception){
            LOGGER.error("database could not save,system automatically switch to json file save mode", e)
            false
        }
    }
}