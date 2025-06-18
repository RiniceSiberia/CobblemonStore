package dev.windmill_broken.cobblemon_store.utils

import dev.windmill_broken.cobblemon_store.CobblemonStore.Companion.LOGGER
import dev.windmill_broken.cobblemon_store.Config
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseUtils {

    val DATABASE : Database  = Database.connect(
            url = Config.DB_PATH + Config.DB_NAME + Config.EXTRA_OPTIONS,
            driver = Config.DB_DRIVER,
            user = Config.DB_USER,
            password = Config.DB_PWD
        )

    val DATABASE_VALID : Boolean = lazy {
        try {
            transaction(DATABASE) {
                exec("SELECT 1")
            }
            true
        }catch (e : Throwable){
//            e.printStackTrace()
            LOGGER.error("数据库未启动",e)
            false
        }
    }.value

}