package com.xxxt.cobblemon_store.store

import java.util.Locale.getDefault

enum class TradeType {
    MONEY,
    ITEM,
    POKEMON
    ;
    val lowercaseName = name.lowercase(getDefault())
}