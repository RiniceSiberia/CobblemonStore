package dev.windmill_broken.cobblemon_store.dao.database

import dev.windmill_broken.cobblemon_store.bo.store.Store
import dev.windmill_broken.cobblemon_store.dao.DAO
import dev.windmill_broken.cobblemon_store.dao.StoresLibrary
import dev.windmill_broken.cobblemon_store.utils.DatabaseUtils

object StoresDBLibrary : StoresLibrary, DAO.DBDAO{

    override val tableName: String
        get() = "cobblemon_store_stores"

    init {
        register()
    }

    override fun createTable(){
        DatabaseUtils.getConnection().use {
            it.prepareStatement("""
                      CREATE TABLE IF NOT EXISTS $tableName (
                          s_id VARCHAR(255) PRIMARY KEY,
                          name TEXT
                      )""".trimIndent()).execute()
        }
    }

    override fun list(): List<Store> {
        val stores = buildList {
            DatabaseUtils.getConnection().use { conn ->
                conn.prepareStatement("SELECT s_id, name FROM store").use { stmt ->
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            val id = rs.getString("s_id")
                            val name = rs.getString("name")
                            add(Store(id,name))
                        }
                    }
                }
            }
        }
        return stores
    }

    override fun get(sid: String): Store? {
        return DatabaseUtils.getConnection().use { conn ->
            val ps = conn.prepareStatement("SELECT * FROM $tableName WHERE s_id = ?")
            ps.setString(1, sid)
            val rs = ps.executeQuery()
            if (rs.next()) {
                Store(rs.getString("s_id"), rs.getString("name"))
            } else {
                null
            }
        }
    }

    override fun createOrUpdate(
        id: String,
        name: String
    ) {
        DatabaseUtils.getConnection().use { conn ->
            val ps = conn.prepareStatement(
                """
                INSERT INTO $tableName (s_id, name)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE
                    name = VALUES(name)"""
            )
            ps.setString(1, id)
            ps.setString(2, name)
            ps.executeUpdate()
        }
    }


    override fun removeById(id: String) {
        DatabaseUtils.getConnection().use { conn ->
            val ps = conn.prepareStatement("DELETE FROM $tableName WHERE s_id = ?")
            ps.setString(1, id)
            ps.executeUpdate()
        }
    }

    override fun register() {
        if (valid && !exists()){
            createTable()
        }
    }

}