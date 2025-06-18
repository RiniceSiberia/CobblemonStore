package dev.windmill_broken.cobblemon_store.utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.dao.StoresLibrary
import dev.windmill_broken.money_lib.dao.json.JsonUtils as MoneyLibJsonUtils
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption


object JsonFileUtils {

    val kJsonConfig get() = Json {  }

    val gsonConfig get() = Gson()

    val rootFolder: Path?
        get() = MoneyLibJsonUtils.rootFolder

    val storesFile: Path
        get() = rootFolder!!.resolve("stores.json")
    val warehousesFile : Path
        get() = rootFolder!!.resolve("warehouses.json")
    val tradesFile : Path
        get() = rootFolder!!.resolve("trades.json")


    fun saveStoresByJson() : Boolean{
        try {
            Files.writeString(
                storesFile,
                    StoresLibrary.let {
                        val ja = JsonArray()

                        it.forEach { it1->
                            ja.add(JsonObject().apply {
                                this.addProperty("id",it1.key)
                                this.add("store",it1.value.serialize())
                            })
                        }
                        Gson().toJson(ja)
                    },
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            CobblemonStore.Companion.LOGGER.info("stores.json 已保存到: {}", storesFile)
            return true
        } catch (e: Exception) {
            CobblemonStore.Companion.LOGGER.error("保存 stores.json 失败", e)
        }
        return false
    }


}