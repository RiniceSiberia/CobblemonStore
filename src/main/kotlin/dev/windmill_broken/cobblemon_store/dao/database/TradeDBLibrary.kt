package dev.windmill_broken.cobblemon_store.dao.database

import dev.windmill_broken.cobblemon_store.CobblemonStore.Companion.LOGGER
import dev.windmill_broken.cobblemon_store.bo.trade.*
import dev.windmill_broken.cobblemon_store.dao.DAO
import dev.windmill_broken.cobblemon_store.dao.TradeLibrary
import dev.windmill_broken.cobblemon_store.utils.DatabaseUtils
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.kJsonConfig
import kotlinx.serialization.builtins.SetSerializer

object TradeDBLibrary : TradeLibrary, DAO.DBDAO {

    init {
        register()
    }

    override val tableName: String
        get() = "cobblemon_store_trades"

    override fun createTable(){

        DatabaseUtils.getConnection().use {
            it.prepareStatement("""
                CREATE TABLE $tableName (
                    t_id INT AUTO_INCREMENT PRIMARY KEY,
                    store_id VARCHAR(255) NOT NULL,
                    creator JSON NOT NULL,
                    auto_remove BOOLEAN NOT NULL,
                    cost JSON NOT NULL,
                    purchasing JSON NOT NULL,
                    store_limits JSON NOT NULL,
                    FOREIGN KEY (store_id) REFERENCES ${StoresDBLibrary.tableName}(s_id)
                        ON DELETE CASCADE ON UPDATE CASCADE
                )
            """).execute()
        }
    }


    override fun get(tradeId: Int): Trade? {
        return DatabaseUtils.getConnection().use { conn ->
            val ps = conn.prepareStatement("SELECT * FROM $tableName WHERE t_id = ?")
            ps.setInt(1, tradeId)
            val rs = ps.executeQuery()
            if (rs.next()) {
                Trade(
                    id = rs.getInt("t_id"),
                    storeId = rs.getString("store_id"),
                    creator = kJsonConfig.decodeFromString(
                        TradeCreator.serializer(),
                        rs.getString("creator")),
                    autoRemove = rs.getBoolean("auto_remove"),
                    cost = kJsonConfig.decodeFromString(
                        Cost.serializer(),
                        rs.getString("cost")
                    ),
                    purchasing = kJsonConfig.decodeFromString(
                        Purchasing.serializer(),
                        rs.getString("purchasing")
                    ),
                    storeLimits = kJsonConfig.decodeFromString(
                        SetSerializer(StoreLimit.serializer()),
                        rs.getString("store_limits")
                    )
                )
            } else {
                null
            }
        }
    }

    override fun create(
        storeId: String,
        creator: TradeCreator,
        autoRemove: Boolean,
        cost: Cost,
        purchasing: Purchasing,
        storeLimits: Set<StoreLimit>
    ): Int {
        val jsonCreator = kJsonConfig.encodeToString(TradeCreator.serializer(),creator)
        val jsonCost = kJsonConfig.encodeToString(Cost.serializer(),cost)
        val jsonPurchasing = kJsonConfig.encodeToString(Purchasing.serializer(),purchasing)
        val jsonLimits = kJsonConfig.encodeToString(SetSerializer(StoreLimit.serializer()),storeLimits.toSet())

        DatabaseUtils.getConnection().use {
            val stmt = it.prepareStatement("""
                INSERT INTO $tableName (store_id, creator, auto_remove, cost, purchasing, store_limits)
                VALUES (?, ?, ?, ?, ?, ?)
            """, java.sql.Statement.RETURN_GENERATED_KEYS)

            stmt.setString(1, storeId)
            stmt.setString(2, jsonCreator)
            stmt.setBoolean(3, autoRemove)
            stmt.setString(4, jsonCost)
            stmt.setString(5, jsonPurchasing)
            stmt.setString(6, jsonLimits)
            stmt.executeUpdate()

            stmt.generatedKeys.use { rs ->
                return if (rs.next()) rs.getInt(1) else -1 // 返回 t_id 或 -1 表示失败
            }
        }
    }

    override fun getByStoreId(storeId: String): List<Trade> {
        val trades = mutableListOf<Trade>()
        DatabaseUtils.getConnection().use { conn ->
            conn.prepareStatement("SELECT * FROM $tableName WHERE store_id = ?").use { stmt ->
                stmt.setString(1, storeId)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        try {
                            val tId = rs.getInt("t_id")
                            val creator = kJsonConfig.decodeFromString(TradeCreator.serializer(),rs.getString("creator"))
                            val autoRemove = rs.getBoolean("auto_remove")
                            val cost = kJsonConfig.decodeFromString(Cost.serializer(),rs.getString("cost"))
                            val purchasing = kJsonConfig.decodeFromString(Purchasing.serializer(),rs.getString("purchasing"))
                            val storeLimits = kJsonConfig.decodeFromString(SetSerializer(StoreLimit.serializer()),rs.getString("store_limits"))
                            trades.add(
                                Trade(
                                    id = tId,
                                    storeId = storeId,
                                    creator = creator,
                                    autoRemove = autoRemove,
                                    cost = cost,
                                    purchasing = purchasing,
                                    storeLimits = storeLimits
                                )
                            )
                        }catch (e : Throwable){
                            LOGGER.error("Error while getting trades", e)
                            continue
                        }
                    }
                }
            }
        }
        return trades
    }

    override fun export(): List<Trade> {
        val trades = mutableListOf<Trade>()
        DatabaseUtils.getConnection().use { conn ->
            conn.prepareStatement("SELECT * FROM $tableName").use { stmt ->
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        val tId = rs.getInt("t_id")
                        val storeId = rs.getString("store_id")
                        val creator = kJsonConfig.decodeFromString(TradeCreator.serializer(),rs.getString("creator"))
                        val autoRemove = rs.getBoolean("auto_remove")
                        val cost = kJsonConfig.decodeFromString(Cost.serializer(),rs.getString("cost"))
                        val purchasing = kJsonConfig.decodeFromString(Purchasing.serializer(),rs.getString("purchasing"))
                        val storeLimits = kJsonConfig.decodeFromString(SetSerializer(StoreLimit.serializer()),rs.getString("store_limits"))
                        trades.add(
                            Trade(
                                id = tId,
                                storeId = storeId,
                                creator = creator,
                                autoRemove = autoRemove,
                                cost = cost,
                                purchasing = purchasing,
                                storeLimits = storeLimits
                            )
                        )
                    }
                }
            }
        }
        return trades
    }

    override fun import(trades: Collection<Trade>) {
        DatabaseUtils.getConnection().use { conn ->
            trades.forEach { trade ->
                val jsonCreator = kJsonConfig.encodeToString(TradeCreator.serializer(),trade.creator)
                val jsonCost = kJsonConfig.encodeToString(Cost.serializer(),trade.cost)
                val jsonPurchasing = kJsonConfig.encodeToString(Purchasing.serializer(),trade.purchasing)
                val jsonLimits = kJsonConfig.encodeToString(SetSerializer(StoreLimit.serializer()),trade.storeLimits.toSet())

                conn.prepareStatement("""
                    INSERT INTO $tableName (store_id, creator, auto_remove, cost, purchasing, store_limits)
                    VALUES (?, ?, ?, ?, ?, ?)
                """).use { stmt ->
                    stmt.setString(1, trade.storeId)
                    stmt.setString(2, jsonCreator)
                    stmt.setBoolean(3, trade.autoRemove)
                    stmt.setString(4, jsonCost)
                    stmt.setString(5, jsonPurchasing)
                    stmt.setString(6, jsonLimits)
                    stmt.executeUpdate()
                }
            }
        }
    }

    override fun update(
        id: Int,
        storeId: String,
        creator: TradeCreator,
        autoRemove: Boolean,
        cost: Cost,
        purchasing: Purchasing,
        storeLimits: Set<StoreLimit>
    ) {
        val jsonCreator = kJsonConfig.encodeToString(TradeCreator.serializer(),creator)
        val jsonCost = kJsonConfig.encodeToString(Cost.serializer(),cost)
        val jsonPurchasing = kJsonConfig.encodeToString(Purchasing.serializer(),purchasing)
        val jsonLimits = kJsonConfig.encodeToString(SetSerializer(StoreLimit.serializer()),storeLimits.toSet())

        DatabaseUtils.getConnection().use {
            val stmt = it.prepareStatement("""
                UPDATE $tableName
                SET creator = ?,store_id =?, auto_remove = ?, cost = ?, purchasing = ?, store_limits = ?
                WHERE t_id = ?
            """)
            stmt.setString(1, jsonCreator)
            stmt.setString(2, storeId)
            stmt.setBoolean(3, autoRemove)
            stmt.setString(4, jsonCost)
            stmt.setString(5, jsonPurchasing)
            stmt.setString(6, jsonLimits)
            stmt.setInt(7, id)

            stmt.executeUpdate() // 返回修改的行数（可能为 0）
        }
    }

    override fun update(trade: Trade) {
        update(
            trade.id,
            trade.storeId,
            trade.creator,
            trade.autoRemove,
            trade.cost,
            trade.purchasing,
            trade.storeLimits
        )
    }

    override fun removeById(id: Int) {
        DatabaseUtils.getConnection().use {
            val stmt = it.prepareStatement("DELETE FROM $tableName WHERE t_id = ?")
            stmt.setInt(1, id)
            stmt.executeUpdate()
        }
    }

    override fun register() {
        if (valid && !exists()){
            createTable()
        }
    }
}