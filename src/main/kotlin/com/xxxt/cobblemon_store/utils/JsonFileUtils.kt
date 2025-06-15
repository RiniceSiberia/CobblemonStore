package com.xxxt.cobblemon_store.utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.xxxt.cobblemon_store.CobblemonStore.Companion.LOGGER
import com.xxxt.cobblemon_store.CobblemonStore.Companion.server
import com.xxxt.cobblemon_store.store.Store
import com.xxxt.cobblemon_store.store.StoresLibrary
import com.xxxt.cobblemon_store.store.WarehouseLibrary
import net.minecraft.world.level.storage.LevelResource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.UUID


object JsonFileUtils {

    val worldRoot: Path?
        get() = server.getWorldPath(LevelResource.ROOT).toAbsolutePath()
    val storesFile: Path
        get() = worldRoot!!.resolve("stores.json")
    val warehouseFile : Path
        get() = worldRoot!!.resolve("warehouse.json")

    fun loadStoresByJson() : Boolean{
        try {
            if (!Files.exists(storesFile)) {
                saveStoresByJson()
            }
            val fileStr = Files.readString(storesFile)
            StoresLibrary.clear()
            val arr = Gson().fromJson(fileStr, JsonArray::class.java)
            for (j in arr.toList()){
                val jo = j.asJsonObject
                val store = Store.deserialize(jo.get("store").asJsonObject)
                val id = jo.get("id").asString
                StoresLibrary[id] = store
            }
            return true
        }catch (e : Exception) {
            LOGGER.error("Failed to read stores.json", e)
        }
        return false
    }

    fun saveStoresByJson() : Boolean{
        try {
            Files.writeString(
                storesFile,
                    StoresLibrary.let {
                        val ja = JsonArray()

                        it.forEach { it1->
                            ja.add(JsonObject().apply {
                                this.addProperty("id",it1.key)
                                this.add("store",it1.value.serialize())
                            })
                        }
                        Gson().toJson(ja)
                    },
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            LOGGER.info("stores.json 已保存到: {}", storesFile)
            return true
        } catch (e: Exception) {
            LOGGER.error("保存 stores.json 失败", e)
        }
        return false
    }

    fun loadWarehouseByJson() : Boolean{
        try {
            if (!Files.exists(storesFile)) {
                saveStoresByJson()
            }
            val fileStr = Files.readString(storesFile)
            WarehouseLibrary.clear()
            val arr = Gson().fromJson(fileStr, JsonArray::class.java)
            for (j in arr.toList()){
                val jo = j.asJsonObject
                val warehouse = WarehouseLibrary.Warehouse.deserialize(jo.get("warehouse").asJsonObject)
                val id = UUID.fromString(jo.get("id").asString)
                WarehouseLibrary[id] = warehouse
            }
            return true
        }catch (e : Exception) {
            LOGGER.error("Failed to read stores.json", e)
        }
        return false
    }

    fun saveWarehouseByJson() : Boolean{
        try {
            Files.writeString(
                warehouseFile,
                WarehouseLibrary.let {
                    val ja = JsonArray()

                    it.forEach { it1->
                        ja.add(JsonObject().apply {
                            this.addProperty("id",it1.key.toString())
                            this.add("warehouse",it1.value.serialize())
                        })
                    }
                    Gson().toJson(ja)
                },
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            LOGGER.info("warehouse.json 已保存到: {}", warehouseFile)
            return true
        } catch (e: Exception) {
            LOGGER.error("保存 warehouse.json 失败", e)
        }
        return false
    }
}