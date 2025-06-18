package dev.windmill_broken.cobblemon_store.utils

import com.google.gson.Gson
import kotlinx.serialization.json.Json
import java.nio.file.Path
import dev.windmill_broken.money_lib.dao.json.JsonUtils as MoneyLibJsonUtils


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

}