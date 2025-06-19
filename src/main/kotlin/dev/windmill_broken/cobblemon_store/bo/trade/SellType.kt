package dev.windmill_broken.cobblemon_store.bo.trade

import java.util.Locale.getDefault

enum class SellType {
    MONEY,
    ITEM,
    POKEMON
    ;
    val lowercaseName = name.lowercase(getDefault())
}