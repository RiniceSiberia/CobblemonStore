package com.xxxt.cobblemon_store.store

import com.xxxt.cobblemon_store.utils.JsonFileUtils
import java.util.concurrent.ConcurrentHashMap

object StoresLibrary : ConcurrentHashMap<String, Store>() {

    private fun readResolve(): Any = StoresLibrary

    init {
        load()
    }

    fun load(): Boolean{
        return if (JsonFileUtils.loadStoresByJson()){
            true
        }else{
            false
        }
    }

    fun save(): Boolean{
        return if (JsonFileUtils.saveStoresByJson()){
            true
        }else{
            false
        }
    }

    fun newStore(id : String,name: String = "Store $id",description: String? = null){
        this[id] = Store(id,name,description)
    }

    fun register(){}
}