package dev.windmill_broken.cobblemon_store.dao.database

import dev.windmill_broken.cobblemon_store.bo.trade.Cost
import dev.windmill_broken.cobblemon_store.bo.trade.Purchasing
import dev.windmill_broken.cobblemon_store.bo.trade.StoreLimit
import dev.windmill_broken.cobblemon_store.bo.trade.Trade
import dev.windmill_broken.cobblemon_store.bo.trade.TradeCreator
import dev.windmill_broken.cobblemon_store.dao.DAO
import dev.windmill_broken.cobblemon_store.dao.TradeLibrary
import dev.windmill_broken.cobblemon_store.dao.database.dto.StoreDBEntity
import dev.windmill_broken.cobblemon_store.dao.database.dto.TradeDBEntity
import dev.windmill_broken.cobblemon_store.dao.database.meta.TradeDBTable
import dev.windmill_broken.cobblemon_store.utils.DatabaseUtils
import dev.windmill_broken.cobblemon_store.utils.MigrationUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

@Suppress("SEALED_INHERITOR_IN_DIFFERENT_PACKAGE")
object TradeDBLibrary : TradeLibrary, DAO.DBDAO {


    init {
        if (valid && StoresDBLibrary.valid){
            transaction(db = DatabaseUtils.DATABASE) {
                MigrationUtils.statementsRequiredForDatabaseMigration(TradeDBTable)
            }
        }
    }

    override fun get(tradeId: Int): Trade? {
        return transaction(db = DatabaseUtils.DATABASE) {
            TradeDBEntity.findById(id = tradeId)?.let {
                Trade(
                    id = it.id.value,
                    storeId = it.store.id.value,
                    creator = it.creator,
                    cost = it.cost,
                    purchasing = it.purchasing,
                    storeLimits = it.storeLimits
                )
            }
        }
    }

    override fun createTrade(
        storeId: String,
        creator: TradeCreator,
        cost: Cost,
        purchasing: Purchasing,
        storeLimits: Set<StoreLimit>
    ) {
        return transaction(db = DatabaseUtils.DATABASE) {
            TradeDBEntity.new{
                this.store = StoreDBEntity.findById(storeId)!!
                this.creator = creator
                this.cost = cost
                this.purchasing = purchasing
                this.storeLimits = storeLimits
            }
        }
    }

    override fun getByStoreId(storeId: String): Collection<Trade> {
        return transaction(db = DatabaseUtils.DATABASE) {
            TradeDBEntity.find{
                TradeDBTable.storeId eq storeId
            }.map{
                Trade(
                    id = it.id.value,
                    storeId = it.store.id.value,
                    creator = it.creator,
                    cost = it.cost,
                    purchasing = it.purchasing,
                    storeLimits = it.storeLimits
                )
            }
        }
    }

    override fun update(trade: Trade) {
        return transaction(db = DatabaseUtils.DATABASE) {
            TradeDBTable.update({ TradeDBTable.id.eq(trade.id) }){
                it[this.storeId] = trade.storeId
                it[this.creator] = trade.creator
                it[this.cost] = trade.cost
                it[this.purchasing] = trade.purchasing
                it[this.storeLimits] = trade.storeLimits
            }
        }
    }

    override fun removeById(id: Int) {
        return transaction(db = DatabaseUtils.DATABASE) {
            TradeDBTable.deleteWhere { TradeDBTable.id eq id }
        }
    }
}