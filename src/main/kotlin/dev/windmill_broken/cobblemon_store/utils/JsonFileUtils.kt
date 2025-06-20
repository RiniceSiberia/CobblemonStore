package dev.windmill_broken.cobblemon_store.utils

import com.google.gson.Gson
import kotlinx.serialization.json.Json
import net.neoforged.fml.loading.FMLPaths
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists


object JsonFileUtils {

    val kJsonConfig get() = Json {
        prettyPrint = true             // ✅ 启用缩进/美化
//        prettyPrintIndent = "  "       // ✅ 设置缩进字符串（默认是两个空格）
        isLenient = true               // 允许宽松语法（如单引号、非引号键）
//        ignoreUnknownKeys = true      // 忽略未知字段
//        encodeDefaults = false        // 忽略默认值字段
//        allowStructuredMapKeys = true // 允许结构化对象作为 map 的 key
//        useArrayPolymorphism = false  // 多态对象是否用数组表示（true 为旧格式）
//        classDiscriminator = "type"   // 设置多态类的区分字段名
//        explicitNulls = false         // 如果为 false，null 不会被序列化
    }

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