package dev.windmill_broken.cobblemon_store.bo.trade

import java.util.Locale.getDefault

enum class TradeType {
    MONEY,
    ITEM,
    POKEMON
    ;
    val lowercaseName = name.lowercase(getDefault())
}