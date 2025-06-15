package com.xxxt.cobblemon_store.store

import com.xxxt.cobblemon_store.utils.JsonFileUtils
import java.util.concurrent.ConcurrentHashMap

object StoresLibrary : ConcurrentHashMap<String, Store>() {

    init {
        load()
    }

    private fun readResolve(): Any = StoresLibrary

    private val stores = ConcurrentHashMap<String, Store>()

    override val size: Int
        get() = stores.size

    override val keys: KeySetView<String?, Store?>
        get() = stores.keys
    override val values: MutableCollection<Store>
        get() = stores.values
    override val entries: MutableSet<MutableMap.MutableEntry<String, Store>>
        get() = stores.entries

    override fun isEmpty(): Boolean = stores.isEmpty()

    override fun containsKey(key: String): Boolean = stores.containsKey(key)

    override fun containsValue(value: Store): Boolean = stores.containsValue(value)

    override fun get(key: String): Store? = stores[key]

    override fun put(
        key: String,
        value: Store
    ): Store? = stores.put(key, value)

    override fun remove(key: String): Store? = stores.remove(key)

    override fun putAll(from: Map<out String, Store>) = stores.putAll(from)

    override fun clear() = stores.clear()


    fun load(): Boolean{
        return if (JsonFileUtils.loadByJson()){
            true
        }else{
            false
        }
    }

    fun save(): Boolean{
        return if (JsonFileUtils.saveByJson()){
            true
        }else{
            false
        }
    }

    fun newStore(id : String,name: String = "Store $id",description: String? = null){
        stores[id] = Store(id,name,description)
    }

    fun register(){}
}