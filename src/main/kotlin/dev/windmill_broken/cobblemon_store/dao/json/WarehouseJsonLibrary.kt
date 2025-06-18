package dev.windmill_broken.cobblemon_store.dao.json

import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.bo.warehouse.Warehouse
import dev.windmill_broken.cobblemon_store.dao.DAO
import dev.windmill_broken.cobblemon_store.dao.WarehouseLibrary
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.kJsonConfig
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.warehousesFile
import dev.windmill_broken.cobblemon_store.utils.serializer.UUIDSerializer
import kotlinx.serialization.builtins.MapSerializer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap


@Suppress("SEALED_INHERITOR_IN_DIFFERENT_PACKAGE")
object WarehouseJsonLibrary : ConcurrentHashMap<UUID, Warehouse>(),
    WarehouseLibrary,
    DAO.JsonDAO {

    init {
        load()
    }

    override fun getOrCreate(pid: UUID): Warehouse {
        if (this[pid] == null)
            put(pid, Warehouse(pid))
        return this[pid]!!
    }

    override fun update(
        pid: UUID,
        warehouse: Warehouse
    ) {
        this[pid] = warehouse
    }

    private fun readResolve(): Any = WarehouseJsonLibrary

    override fun load() {
        try {
            if (!Files.exists(warehousesFile)) {
                save()
            }
            val fileStr = Files.readString(
                warehousesFile,
                StandardCharsets.UTF_8
                )
            val map = kJsonConfig.decodeFromString(
                MapSerializer(
                    UUIDSerializer,
                    Warehouse.serializer()
                ),fileStr)
            this.clear()
            this.putAll(map)
        }catch (e : Exception) {
            CobblemonStore.Companion.LOGGER.error("Failed to read warehousesFile.json", e)
        }
    }

    override fun save() {
        try {
            Files.writeString(
                warehousesFile,
                kJsonConfig.encodeToString(
                        MapSerializer(
                            UUIDSerializer,
                            Warehouse.serializer()
                        ),this),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            CobblemonStore.Companion.LOGGER.info("warehouse.json 已保存到: {}", warehousesFile)
        } catch (e: Exception) {
            CobblemonStore.Companion.LOGGER.error("保存 warehouse.json 失败", e)
        }
    }
}