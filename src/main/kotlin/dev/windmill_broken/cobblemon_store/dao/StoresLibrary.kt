package dev.windmill_broken.cobblemon_store.dao

import dev.windmill_broken.cobblemon_store.bo.store.Store

interface StoresLibrary : DAO {

    operator fun get(sid : String) : Store?

    fun create(
        id : String,
        name : String,
        description: String? = null
        )

    fun update(id : String, store : Store)

    fun removeById(id : String)

    fun register(){}
}