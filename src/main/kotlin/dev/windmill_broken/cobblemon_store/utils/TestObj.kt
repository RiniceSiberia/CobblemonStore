package dev.windmill_broken.cobblemon_store.utils

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table

object TestObj {

    fun register(){}

    fun test(){
        println("testDx3906:${Table.Dual.tableName}")
        try {
            Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver", user = "root", password = "")
        }catch (e : Throwable){
            e.printStackTrace()
        }
    }
}