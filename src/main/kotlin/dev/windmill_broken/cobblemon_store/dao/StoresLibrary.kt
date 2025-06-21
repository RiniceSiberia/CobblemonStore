package dev.windmill_broken.cobblemon_store.dao

import dev.windmill_broken.cobblemon_store.bo.store.Store

interface StoresLibrary : DAO {

    fun list() : List<Store>

    operator fun get(sid : String) : Store?

    fun createOrUpdate(
        id : String,
        name : String
        )

    fun removeById(id : String)
}