package com.xxxt.cobblemon_store.store

import com.google.protobuf.Descriptors
import kotlinx.serialization.Serializable

@Serializable
class Store(
    val id : Int,
    var name : String = "Store $id",
    val description: String?,
    val trades : MutableList<Trade> = mutableListOf()
) : MutableList<Trade> by trades{
}