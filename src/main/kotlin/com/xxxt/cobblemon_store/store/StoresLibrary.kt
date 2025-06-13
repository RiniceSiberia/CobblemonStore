package com.xxxt.cobblemon_store.store

import com.xxxt.cobblemon_store.utils.DataBaseUtils
import com.xxxt.cobblemon_store.utils.JsonFileUtils
import java.util.concurrent.ConcurrentHashMap

object StoresLibrary : ConcurrentHashMap<Int, Store>() {

    init {
        load()
    }

    private fun readResolve(): Any = StoresLibrary

    private val stores = ConcurrentHashMap<Int, Store>()

    override val size: Int
        get() = stores.size

    override val keys: KeySetView<Int?, Store?>
        get() = stores.keys
    override val values: MutableCollection<Store>
        get() = stores.values
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Store>>
        get() = stores.entries

    override fun isEmpty(): Boolean = stores.isEmpty()

    override fun containsKey(key: Int): Boolean = stores.containsKey(key)

    override fun containsValue(value: Store): Boolean = stores.containsValue(value)

    override fun get(key: Int): Store? = stores[key]

    override fun put(
        key: Int,
        value: Store
    ): Store? = stores.put(key, value)

    override fun remove(key: Int): Store? = stores.remove(key)

    override fun putAll(from: Map<out Int, Store>) = stores.putAll(from)

    override fun clear() = stores.clear()


    fun load(): Boolean{
        return if (DataBaseUtils.loadByDataBase()){
            true
        }else if (JsonFileUtils.loadByJson()){
            true
        }else{
            false
        }
    }

    fun save(): Boolean{
        return if (DataBaseUtils.saveByDataBase()){
            true
        }else if (JsonFileUtils.saveByJson()){
            true
        }else{
            false
        }
    }

    fun register(){}
}