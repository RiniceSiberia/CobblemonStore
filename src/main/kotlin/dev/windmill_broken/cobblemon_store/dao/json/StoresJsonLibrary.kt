package dev.windmill_broken.cobblemon_store.dao.json

import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.bo.store.Store
import dev.windmill_broken.cobblemon_store.dao.DAO
import dev.windmill_broken.cobblemon_store.dao.StoresLibrary
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.kJsonConfig
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.storesFile
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap

object StoresJsonLibrary : ConcurrentHashMap<String, Store>(), StoresLibrary, DAO.JsonDAO {

    private fun readResolve(): Any = StoresJsonLibrary

    override fun list(): List<Store> {
        return this.values.toList()
    }

    override fun get(sid: String): Store? = super.get(sid)

    override fun createOrUpdate(
        id: String,
        name: String

    ) {
        val store = Store(id,name)
        this[store.id] = store
    }

    override fun removeById(id: String) {
        this.remove(id)
    }

    override fun save() {
        try {
            Files.writeString(
                storesFile,
                kJsonConfig.encodeToString(
                    MapSerializer(
                        String.serializer(),
                        Store.serializer()
                    ),this.toMap()),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            CobblemonStore.Companion.LOGGER.info("storesFile.json 已保存到: {}", storesFile)
        } catch (e: Exception) {
            CobblemonStore.Companion.LOGGER.error("保存 storesFile.json 失败", e)
        }
    }

    override fun load() {
        try{
            if (!Files.exists(storesFile)) {
                save()
            }
            val fileStr = Files.readString(
                storesFile,
                StandardCharsets.UTF_8)
            val map = kJsonConfig.decodeFromString(
                MapSerializer(
                    String.serializer(),
                    Store.serializer()
                ),fileStr)
            this.clear()
            this.putAll(map)
        }catch (e : Exception) {
            CobblemonStore.Companion.LOGGER.error("Failed to read stores.json", e)
        }
    }
}