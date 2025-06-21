package dev.windmill_broken.cobblemon_store.dao

import dev.windmill_broken.cobblemon_store.bo.warehouse.Warehouse
import java.util.*

/**
 * 玩家仓库
 */

interface WarehouseLibrary : DAO{

    fun getOrCreate(pid : UUID) : Warehouse

    fun update(pid : UUID, warehouse : Warehouse)
}


