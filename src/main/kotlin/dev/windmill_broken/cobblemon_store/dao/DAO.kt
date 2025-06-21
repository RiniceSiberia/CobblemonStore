package dev.windmill_broken.cobblemon_store.dao

import dev.windmill_broken.cobblemon_store.utils.DatabaseUtils


interface DAO {

    val valid : Boolean
        get() = true

    interface DBDAO : DAO{

        val tableName : String

        override val valid: Boolean
            get() = DatabaseUtils.DATABASE_VALID


        fun exists(): Boolean {
            return DatabaseUtils.tableExists(this)
        }

        fun createTable()

        fun register()
    }

    interface JsonDAO : DAO{

        fun save()

        fun load()
    }
}