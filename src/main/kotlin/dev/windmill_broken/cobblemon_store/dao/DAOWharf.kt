package dev.windmill_broken.cobblemon_store.dao

import dev.windmill_broken.cobblemon_store.dao.database.StoresDBLibrary
import dev.windmill_broken.cobblemon_store.dao.database.TradeDBLibrary
import dev.windmill_broken.cobblemon_store.dao.database.WarehouseDBLibrary
import dev.windmill_broken.cobblemon_store.dao.json.StoresJsonLibrary
import dev.windmill_broken.cobblemon_store.dao.json.TradeJsonLibrary
import dev.windmill_broken.cobblemon_store.dao.json.WarehouseJsonLibrary
import dev.windmill_broken.money_lib.dao.DAO
import dev.windmill_broken.money_lib.dao.database.DatabaseUtils

object DAOWharf {
    val warehouseLibrary : WarehouseLibrary
        get() = if (DatabaseUtils.DATABASE_VALID){
            WarehouseDBLibrary
        }else{
            WarehouseJsonLibrary
        }

    val storeLibrary : StoresLibrary
        get() = if (DatabaseUtils.DATABASE_VALID){
            StoresDBLibrary
        }else{
            StoresJsonLibrary
        }

    val tradeLibrary : TradeLibrary
        get() = if (DatabaseUtils.DATABASE_VALID){
            TradeDBLibrary
        }else{
            TradeJsonLibrary
        }

    fun save(){
        val list = listOf(warehouseLibrary, storeLibrary, tradeLibrary)
        list.forEach {
            if (it is DAO.JsonDAO){
                save()
            }
        }
    }
    fun load(){
        val list = listOf(warehouseLibrary, storeLibrary, tradeLibrary)
        list.forEach {
            if (it is DAO.JsonDAO){
                load()
            }
        }
    }
}