package com.xxxt.cobblemon_store.utils

import com.xxxt.cobblemon_store.store.Store
import com.xxxt.cobblemon_store.store.StoresLibrary
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption


object JsonFileUtils {

    val jsonConfig = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    lateinit var server: MinecraftServer
    val worldRoot: Path?
        get() = server.getWorldPath(LevelResource.ROOT).toAbsolutePath()
    val storesFile: Path
        get() = worldRoot!!.resolve("stores.json")

    fun loadByJson() : Boolean{
        try {
            assert(Files.exists(storesFile))
            val fileStr = Files.readString(storesFile)
            val map = jsonConfig.decodeFromString(
                MapSerializer(
                    Int.serializer(),
                    Store.serializer()),
                fileStr)
                StoresLibrary.clear()
            map.forEach { key, value ->
                StoresLibrary[key] = value
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
                jsonConfig.encodeToString(
                    MapSerializer(Int.serializer(),Store.serializer()),
                    StoresLibrary
                ),
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