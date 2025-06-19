package dev.windmill_broken.cobblemon_store.utils

import com.google.gson.Gson
import kotlinx.serialization.json.Json
import net.neoforged.fml.loading.FMLPaths
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists


object JsonFileUtils {

    val kJsonConfig get() = Json {  }

    val gsonConfig get() = Gson()

    val rootFolder: Path?
        get() {
            val gameDir = FMLPaths.GAMEDIR.get()
            val myGlobalJsonDir = gameDir.resolve("windmill_broken_data")
            if (!myGlobalJsonDir.exists()){
                Files.createDirectories(myGlobalJsonDir)
            }
            return myGlobalJsonDir
        }

    val storesFile: Path
        get() = rootFolder!!.resolve("stores.json")
    val warehousesFile : Path
        get() = rootFolder!!.resolve("warehouses.json")
    val tradesFile : Path
        get() = rootFolder!!.resolve("trades.json")

}