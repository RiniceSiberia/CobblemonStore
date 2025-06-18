package dev.windmill_broken.cobblemon_store.dao.database

import dev.windmill_broken.cobblemon_store.bo.warehouse.Warehouse
import dev.windmill_broken.cobblemon_store.dao.WarehouseLibrary
import dev.windmill_broken.cobblemon_store.dao.database.dto.WarehouseLibraryDBEntity
import dev.windmill_broken.cobblemon_store.dao.database.meta.WarehouseLibraryDBTable
import dev.windmill_broken.money_lib.dao.DAO
import dev.windmill_broken.money_lib.dao.database.DatabaseUtils
import dev.windmill_broken.money_lib.dao.database.MigrationUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

@Suppress("SEALED_INHERITOR_IN_DIFFERENT_PACKAGE")
object WarehouseDBLibrary : WarehouseLibrary, DAO.DBDAO{

    init {
        if (valid){
            transaction(db = DatabaseUtils.DATABASE) {
                MigrationUtils.statementsRequiredForDatabaseMigration(WarehouseLibraryDBTable)
            }
        }
    }


    override fun getOrCreate(pid: UUID): Warehouse {
        return transaction(db = DatabaseUtils.DATABASE) {
            val dto = WarehouseLibraryDBEntity.findById(pid)?:let {
                WarehouseLibraryDBEntity.new(pid){
                    warehouse = Warehouse(pid)
                }
            }
            dto.warehouse
        }
    }

    override fun update(pid: UUID, warehouse: Warehouse) {
        return transaction(db = DatabaseUtils.DATABASE) {
            WarehouseLibraryDBTable.update({
                WarehouseLibraryDBTable.id eq pid
            }){
                with(SqlExpressionBuilder) {
                    it[WarehouseLibraryDBTable.warehouse] = warehouse
                }
            }
        }
    }
}