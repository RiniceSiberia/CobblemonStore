package dev.windmill_broken.cobblemon_store.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.windmill_broken.cobblemon_store.CobblemonStore.Companion.LOGGER
import dev.windmill_broken.cobblemon_store.Config
import dev.windmill_broken.cobblemon_store.dao.DAO
import java.sql.Connection
import javax.sql.DataSource

object DatabaseUtils {

    private val config = HikariConfig().apply {
        jdbcUrl = Config.DB_PATH + Config.DB_NAME + Config.EXTRA_OPTIONS
        username = Config.DB_USER
        password = Config.DB_PWD
        driverClassName = Config.DB_DRIVER
        maximumPoolSize = 10
    }

    fun tableExists(dao : DAO.DBDAO): Boolean {
        dataSource.connection.use { conn ->
            val meta = conn.metaData
            meta.getTables(null, null, dao.tableName, null).use { rs ->
                return rs.next()
            }
        }
    }

    val dataSource: DataSource = HikariDataSource(config)
    fun getConnection(): Connection = dataSource.connection

    val DATABASE_VALID: Boolean
        get() = try {
            getConnection().use { conn ->
                // 推荐使用 isValid 方法判断连接有效性（5秒超时）
                conn.isValid(5)
            }
        } catch (e: Exception) {
//            e.printStackTrace()
            LOGGER.info("Database connection failed.Strengthen the number of uses and keep the number of progress!",e)
            false
        }

}