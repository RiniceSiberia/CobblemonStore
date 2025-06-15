package com.xxxt.cobblemon_store

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.neoforge.common.ModConfigSpec

class Config {


    companion object {

        private val BUILDER: ModConfigSpec.Builder = ModConfigSpec.Builder()

        private val MYSQL_PATH: ModConfigSpec.ConfigValue<String> = BUILDER
            .define("mysql_path", "jdbc:mysql://localhost:3306/")

        private val MYSQL_DRIVER: ModConfigSpec.ConfigValue<String> = BUILDER
            .define("mysql_driver", "com.mysql.cj.jdbc.Driver")

        private val MYSQL_USER: ModConfigSpec.ConfigValue<String> = BUILDER
            .define("mysql_user", "root")

        private val MYSQL_PASSWORD: ModConfigSpec.ConfigValue<String> = BUILDER
            .define("mysql_password", "123456")

        private val DB_NAME: ModConfigSpec.ConfigValue<String> = BUILDER
            .define("db_name", "mydb")

        private val EXTRA_OPTIONS: ModConfigSpec.ConfigValue<String> = BUILDER
            .define("extra_options", "?useSSL=false&serverTimezone=UTC")

        val spec: ModConfigSpec = BUILDER.build()

        lateinit var mysql_path: String
        lateinit var mysql_driver: String
        lateinit var mysql_user: String
        lateinit var mysql_password: String
        lateinit var db_name: String
        lateinit var extra_options: String

        @SubscribeEvent
        fun onLoad(event: ModConfigEvent) {
            mysql_path = MYSQL_PATH.get()
            mysql_driver = MYSQL_DRIVER.get()
            mysql_user = MYSQL_USER.get()
            mysql_password = MYSQL_PASSWORD.get()
            db_name = DB_NAME.get()
            extra_options = EXTRA_OPTIONS.get()
        }
    }

}