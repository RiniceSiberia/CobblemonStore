package dev.windmill_broken.cobblemon_store.utils

import com.mojang.logging.LogUtils
import net.minecraft.world.entity.player.Player
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.slf4j.Logger
import java.lang.Double
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import kotlin.Boolean
import kotlin.NumberFormatException
import kotlin.String
import kotlin.Throws
import kotlin.isNaN

object PluginUtils {
    private const val GTS = "Gts"

    private val LOGGER: Logger = LogUtils.getLogger()


    fun getPlugin(plugin: String): Plugin? {
        return Bukkit.getPluginManager().getPlugin(plugin)
    }

    val pluginClass: Class<*>
        get() = getPlugin(GTS)!!.javaClass

    @Throws(NoSuchMethodException::class)
    fun getMethod(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>?): Method {
        val method = clazz.getDeclaredMethod(methodName, *parameterTypes)
        method.setAccessible(true)
        return method
    }

    fun getValut(player: Player): kotlin.Double? {
        return getValut(player.getUUID())
    }

    /**
     * 获得玩家的钱
     * @param player 玩家的ID
     * @return 玩家的钱
     */
    fun getValut(player: UUID): kotlin.Double? {
        try {
            val clazz = pluginClass
            val method = getMethod(clazz, "getBalance", UUID::class.java)
            val value = (method.invoke(clazz, player) as Double).toDouble()
            if (value.isNaN()){
                return null
            }else{
                return value
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return null
    }

    fun addMoney(player: Player, price: kotlin.Double) : Boolean{
        return addMoney(player.getUUID(), price)
    }

    fun addMoney(player: UUID, price: kotlin.Double): Boolean {
        try {
            if (!price.isNaN()) throw NumberFormatException("Price is not a number")
            val clazz = pluginClass
            val method = getMethod(clazz, "depositPlayer", UUID::class.java, Double.TYPE)
            method.invoke(clazz, player, price)
            return true
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            return false
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            return false
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            return false
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return false
        }
    }

    fun minusMoney(player: Player, price: kotlin.Double) : Boolean{
        return minusMoney(player.uuid,price)
    }


    fun minusMoney(userUuid: UUID?, price: kotlin.Double): Boolean {
        try {
            if (!price.isNaN()) throw NumberFormatException("Price is not a number")
            val clazz = pluginClass
            val method = getMethod(clazz, "withdrawPlayer", UUID::class.java, Double.TYPE)
            method.invoke(clazz, userUuid, price)
            return true
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            return false
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            return false
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            return false
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return false
        }
    }

    fun checkBukkitInstalled(): Boolean {
        try {
            val server = Bukkit.getServer()
            return true
        } catch (e: NoClassDefFoundError) {
            this.LOGGER.error("No Bukkit installed on this platform")
        }
        return false
    }

}