package com.xxxt.cobblemon_store.utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.xxxt.cobblemon_store.CobblemonStore.Companion.LOGGER
import com.xxxt.cobblemon_store.store.Store
import com.xxxt.cobblemon_store.store.StoresLibrary
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption


object JsonFileUtils {

    lateinit var server: MinecraftServer
    val worldRoot: Path?
        get() = server.getWorldPath(LevelResource.ROOT).toAbsolutePath()
    val storesFile: Path
        get() = worldRoot!!.resolve("stores.json")

    fun loadByJson() : Boolean{
        try {
            if (!Files.exists(storesFile)) {
                saveByJson()
            }
            val fileStr = Files.readString(storesFile)
            StoresLibrary.clear()
            val arr = Gson().fromJson(fileStr, JsonArray::class.java)
            for (j in arr.toList()){
                val jo = j.asJsonObject
                val store = Store.deserialize(jo.get("store").asJsonObject)
                val index = jo.get("id").asInt
                StoresLibrary[index] = store
            }
            return true
        }catch (e : Exception) {
            LOGGER.error("Failed to read stores.json", e)
        }
        return false
    }

    fun saveByJson() : Boolean{
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
}