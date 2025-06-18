package dev.windmill_broken.cobblemon_store.dao


interface DAO {

    val valid : Boolean
        get() = true

    interface DBDAO : DAO{
        override val valid: Boolean
            get() = DatabaseUtils.DATABASE_VALID
    }

    interface JsonDAO : DAO{

        fun save()

        fun load()
    }
}