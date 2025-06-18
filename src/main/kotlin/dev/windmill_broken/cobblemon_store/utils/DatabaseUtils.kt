package dev.windmill_broken.cobblemon_store.utils

import dev.windmill_broken.cobblemon_store.Config
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseUtils {
    val DATABASE : Database  = Database.connect(
            url = Config.DB_PATH + Config.DB_NAME + Config.DB_EXTRA_OPTIONS,
            driver = Config.DB_DRIVER,
            user = Config.DB_USERNAME,
            password = Config.DB_PWD
        )
    val DATABASE_VALID : Boolean
        get() = try {
            transaction(DATABASE) {
                exec("SELECT 1")
            }
            true
        }catch (e : Throwable){
//            e.printStackTrace()
            LOGGER.info("数据库未启动")
            false
        }

}