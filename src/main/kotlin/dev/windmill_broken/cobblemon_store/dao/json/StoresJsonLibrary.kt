package dev.windmill_broken.cobblemon_store.dao.json

import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.bo.store.Store
import dev.windmill_broken.cobblemon_store.bo.trade.CostObj
import dev.windmill_broken.cobblemon_store.bo.trade.PurchasingObj
import dev.windmill_broken.cobblemon_store.bo.trade.StoreLimit
import dev.windmill_broken.cobblemon_store.bo.trade.Trade
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import dev.windmill_broken.cobblemon_store.dao.StoresLibrary
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.storesFile
import dev.windmill_broken.money_lib.dao.DAO
import dev.windmill_broken.money_lib.dao.json.JsonUtils.jsonConfig
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap

object StoresJsonLibrary : ConcurrentHashMap<String, Store>(), StoresLibrary, DAO.JsonDAO {

    private fun readResolve(): Any = StoresJsonLibrary

    init {
        load()
    }

    override fun get(sid: String): Store? = this[sid]

    override fun create(
        id: String,
        name: String,
        description: String?,
        tradeValues: List<Triple<CostObj, PurchasingObj, Map<String, StoreLimit>>>

    ) {
        val store = Store(id,name,description)
        this[store.id] = store
        tradeValues.forEach { (cost,purchasing,limit) ->
            DAOWharf.tradeLibrary.createTrade(id,cost,purchasing,limit)
        }
    }

    override fun update(id: String, store: Store){
        this[id] = store
        save()
    }

    override fun removeById(id: String) {
        this.remove(id)
    }

    override fun save() {
        try {
            Files.writeString(
                storesFile,
                jsonConfig.encodeToString(
                    MapSerializer(
                        String.serializer(),
                        Store.serializer()
                    ),this),
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
            val map = jsonConfig.decodeFromString(
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