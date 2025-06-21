package dev.windmill_broken.cobblemon_store

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.neoforge.common.ModConfigSpec

object Config {

    private val BUILDER: ModConfigSpec.Builder = ModConfigSpec.Builder()

    private val dbPath: ModConfigSpec.ConfigValue<String> = BUILDER
        .define("db_path", "jdbc:mysql://localhost:3306/")

    private val dbDriver: ModConfigSpec.ConfigValue<String> = BUILDER
        .define("db_driver", "com.mysql.cj.jdbc.Driver")

    private val dbUser: ModConfigSpec.ConfigValue<String> = BUILDER
        .define("db_user", "root")

    private val dbPwd: ModConfigSpec.ConfigValue<String> = BUILDER
        .define("db_pwd", "123456")

    private val dbName: ModConfigSpec.ConfigValue<String> = BUILDER
        .define("db_name", "mydb")

    private val extraOptions: ModConfigSpec.ConfigValue<String> = BUILDER
        .define("extra_options", "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC")




    val spec: ModConfigSpec = BUILDER.build()

    lateinit var DB_PATH: String
    lateinit var DB_DRIVER: String
    lateinit var DB_USER: String
    lateinit var DB_PWD: String
    lateinit var DB_NAME: String
    lateinit var EXTRA_OPTIONS: String

    fun register(modEventBus: IEventBus, modContainer: ModContainer) {
//        container.registerConfig(ModConfig.Type.SERVER, SPEC,"${MOD_FOLDER_ID}/money_settings_server.cfg")
        modContainer.registerConfig(ModConfig.Type.SERVER, spec)
        with(modEventBus){
            addListener { event : ModConfigEvent.Loading ->
                DB_PATH = dbPath.get()
                DB_DRIVER = dbDriver.get()
                DB_USER = dbUser.get()
                DB_PWD = dbPwd.get()
                DB_NAME = dbName.get()
                EXTRA_OPTIONS = extraOptions.get()
            }
        }
    }

}