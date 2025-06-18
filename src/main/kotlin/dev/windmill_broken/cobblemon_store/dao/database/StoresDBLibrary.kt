package dev.windmill_broken.cobblemon_store.dao.database

import dev.windmill_broken.cobblemon_store.bo.store.Store
import dev.windmill_broken.cobblemon_store.bo.trade.CostObj
import dev.windmill_broken.cobblemon_store.bo.trade.PurchasingObj
import dev.windmill_broken.cobblemon_store.bo.trade.StoreLimit
import dev.windmill_broken.cobblemon_store.dao.StoresLibrary
import dev.windmill_broken.cobblemon_store.dao.database.dto.StoreDBEntity
import dev.windmill_broken.cobblemon_store.dao.database.meta.StoreDBTable
import dev.windmill_broken.cobblemon_store.dao.database.meta.TradeDBTable
import dev.windmill_broken.money_lib.dao.DAO
import dev.windmill_broken.money_lib.dao.database.DatabaseUtils
import dev.windmill_broken.money_lib.dao.database.MigrationUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

@Suppress("SEALED_INHERITOR_IN_DIFFERENT_PACKAGE")
object StoresDBLibrary : StoresLibrary, DAO.DBDAO{
    init {
        if (TradeDBLibrary.valid && valid){
            transaction(db = DatabaseUtils.DATABASE) {
                MigrationUtils.statementsRequiredForDatabaseMigration(TradeDBTable)
            }
        }
    }
    override fun get(sid: String): Store? {
        return transaction(db = DatabaseUtils.DATABASE) {
            StoreDBEntity.findById(sid)?.let {
                Store(
                    id = it.id.value,
                    name = it.name,
                    description = it.description
                )
            }
        }
    }

    override fun create(
        id: String,
        name: String,
        description: String?,
        tradeValues: List<Triple<CostObj, PurchasingObj, Map<String, StoreLimit>>>
    ) {
        transaction(db = DatabaseUtils.DATABASE) {
            StoreDBEntity.new(id) {
                this.name = name
                this.description = description
            }
            tradeValues.forEach { (cost,purchasing,storeLimits) ->
                TradeDBLibrary.createTrade(id,cost,purchasing,storeLimits)
            }
        }
    }

    override fun update(id: String, store: Store) {
        return transaction(db = DatabaseUtils.DATABASE) {
            StoreDBTable.update({ StoreDBTable.id eq id }) {
                it[name] = store.name
                it[description] = store.description
            }
        }
    }

    override fun removeById(id: String) {
        return transaction(db = DatabaseUtils.DATABASE) {
            StoreDBEntity.findById(id)?.delete()
        }
    }

}