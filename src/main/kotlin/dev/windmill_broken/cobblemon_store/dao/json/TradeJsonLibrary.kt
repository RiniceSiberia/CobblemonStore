package dev.windmill_broken.cobblemon_store.dao.json

import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.bo.trade.Cost
import dev.windmill_broken.cobblemon_store.bo.trade.Purchasing
import dev.windmill_broken.cobblemon_store.bo.trade.StoreLimit
import dev.windmill_broken.cobblemon_store.bo.trade.Trade
import dev.windmill_broken.cobblemon_store.dao.DAO
import dev.windmill_broken.cobblemon_store.dao.TradeLibrary
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.kJsonConfig
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.tradesFile
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap

object TradeJsonLibrary: ConcurrentHashMap<Int, Trade>(), TradeLibrary, DAO.JsonDAO {

    private fun readResolve(): Any = TradeJsonLibrary
    override fun get(tradeId: Int): Trade? {
        return super.get(tradeId)
    }

    override fun createTrade(
        storeId: String,
        cost: Cost,
        purchasing: Purchasing,
        storeLimits: Set<StoreLimit>
    ) {
        val t = Trade(
            nextEmptyIndex,
            storeId,
            cost,
            purchasing,
            storeLimits
        )
        this[t.id] = t
    }

    val nextEmptyIndex : Int
        get(){
            var i = 0
            while (this.containsKey(i)) {
                i++
            }
            return i
        }

    override fun getByStoreId(storeId: String): Collection<Trade> {
        return this.values.filter {
            it.storeId == storeId
        }
    }

    override fun update(trade: Trade) {
        this[trade.id] = trade
    }

    override fun removeById(id: Int) {
        this.remove(id)
    }

    override fun load() {
        try {
            if (!Files.exists(tradesFile)) {
                WarehouseJsonLibrary.save()
            }
            val fileStr = Files.readString(
                tradesFile,
                StandardCharsets.UTF_8
                )
            val map = kJsonConfig.decodeFromString(
                MapSerializer(
                    Int.serializer(),
                    Trade.serializer()
                ),fileStr)
            this.clear()
            this.putAll(map)
        }catch (e : Exception) {
            CobblemonStore.Companion.LOGGER.error("Failed to read tradesFile.json", e)
        }
    }

    override fun save() {
        try {
            Files.writeString(
                tradesFile,
                kJsonConfig.encodeToString(
                    MapSerializer(
                        Int.serializer(),
                        Trade.serializer()
                    ),
                    this.toMap()
                ),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            CobblemonStore.Companion.LOGGER.info("tradesFile.json 已保存到: {}", tradesFile)
        }catch (e : Exception){
            CobblemonStore.Companion.LOGGER.error("保存 tradesFile.json 失败", e)
        }
    }

}