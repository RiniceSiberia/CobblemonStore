package com.xxxt.cobblemon_store.store

import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import com.xxxt.cobblemon_store.CobblemonStore.Companion.LOGGER
import com.xxxt.cobblemon_store.CobblemonStore.Companion.registryAccess
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import kotlin.isInfinite
import kotlin.isNaN

sealed class PurchasingObj{

    val successMsgPath = "msg.cobblemon_store.purchasing.${type.lowercaseName}"

    val tooltipMsgPath = "msg.cobblemon_store.slot.purchasing.${type.lowercaseName}"

    abstract val type : TradeType

    abstract fun purchasing(player : Player) : WarehouseLibrary.Warehouse.WarehouseItem

    abstract fun purchasingMsgComponent() : MutableComponent

    abstract fun purchasingTooltipComponent() : MutableComponent

    fun serialize() : JsonElement{
        return JsonObject().apply {
            addProperty("name","purchasing")
            addProperty("type",type.lowercaseName)
            serialize()
        }
    }

    abstract fun JsonObject.serialize()

    companion object{
        fun deserialize(json : JsonObject) : PurchasingObj?{
            return try {
                val name = json.get("name").asString
                assert(name == "purchasing")
                val type = json.asJsonObject.get("type").asString
                when(type){
                    TradeType.MONEY.lowercaseName ->{
                        MoneyPurchasingObj.deserialize(json)
                    }
                    TradeType.ITEM.lowercaseName ->{
                        ItemPurchasingObj.deserialize(json)
                    }
                    TradeType.POKEMON.lowercaseName ->{
                        PokemonPurchasingObj.deserialize(json)
                    }
                    else -> null
                }
                null
            }catch (e : Throwable){
                e.printStackTrace()
                null
            }
        }
    }

}
class ItemPurchasingObj(
        val stack : ItemStack
    ) : PurchasingObj(){

        override val type: TradeType
            get() = TradeType.ITEM

        override fun purchasing(player: Player): WarehouseLibrary.Warehouse.WarehouseItem.ItemWarehouseItem {
            val warehouse = WarehouseLibrary.getOrCreate(player)
            return WarehouseLibrary.Warehouse.WarehouseItem.ItemWarehouseItem(
                player.uuid,
                warehouse.nextEmptyIndex,
                stack
            )
        }

        override fun purchasingMsgComponent(): MutableComponent {
            return Component.translatable(successMsgPath,stack.hoverName,stack.count)
        }

        override fun purchasingTooltipComponent(): MutableComponent {
            return Component.translatable(
                tooltipMsgPath,
                stack.displayName,
                stack.count
            )
        }

        override fun JsonObject.serialize() {
            val nbt = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE,this@ItemPurchasingObj.stack)
                .resultOrPartial{err ->
                    LOGGER.error("item stack序列化出错:${err}")
                }.orElseThrow()
            add("nbt",nbt)
        }

        companion object{
            fun deserialize(json : JsonObject) : ItemPurchasingObj?{
                return try {
                    val stack = ItemStack.CODEC.parse(JsonOps.INSTANCE,json.getAsJsonObject("nbt"))
                        .resultOrPartial{err ->
                            LOGGER.error("item stack反序列化出错:${err}")
                        }.orElseThrow()
                    ItemPurchasingObj(stack)
                }catch (e : Throwable){
                    e.printStackTrace()
                    null
                }
            }
        }
    }

class MoneyPurchasingObj(
    val value : Double
) : PurchasingObj(){

    init {
        if (value.isNaN() || value.isInfinite()){
            0.0
        }else{
            value
        }
    }

    override val type: TradeType
        get() = TradeType.MONEY

    override fun purchasing(player: Player): WarehouseLibrary.Warehouse.WarehouseItem.MoneyWarehouseItem {
        val warehouse = WarehouseLibrary.getOrCreate(player)
        return WarehouseLibrary.Warehouse.WarehouseItem.MoneyWarehouseItem(
            player.uuid,
            warehouse.nextEmptyIndex,
            value
        )
    }

    override fun purchasingMsgComponent(): MutableComponent {
        return Component.translatable(successMsgPath, String.format("%.2f", value))
    }

    override fun purchasingTooltipComponent(): MutableComponent {
        return Component.translatable(
            tooltipMsgPath,
            String.format("%.2f", value)
        )
    }


    override fun JsonObject.serialize() {
        addProperty("value",value)
    }

    companion object{
        fun deserialize(json : JsonObject) : MoneyPurchasingObj?{
            return try {
                val value = json.getAsJsonPrimitive("value").asDouble
                MoneyPurchasingObj(value)
            }catch (e : Throwable){
                e.printStackTrace()
                null
            }
        }
    }
}
class PokemonPurchasingObj(
    val pokemon : Pokemon
) : PurchasingObj(){

    override val type: TradeType
        get() = TradeType.POKEMON

    override fun purchasing(player: Player): WarehouseLibrary.Warehouse.WarehouseItem {
        val warehouse = WarehouseLibrary.getOrCreate(player)
        return WarehouseLibrary.Warehouse.WarehouseItem.PokemonWarehouseItem(
            player.uuid,
            warehouse.nextEmptyIndex,
            pokemon
        )
    }

    override fun purchasingMsgComponent(): MutableComponent {
        return Component.translatable(successMsgPath,pokemon.getDisplayName())
    }

    override fun purchasingTooltipComponent(): MutableComponent {
        return Component.translatable(
            tooltipMsgPath,
            pokemon.getDisplayName()
        )
    }


    override fun JsonObject.serialize() {
        add("pokemon",pokemon.saveToJSON(registryAccess))
    }
    companion object{
        fun deserialize(json : JsonObject) : PokemonPurchasingObj?{
            return try {
                val pokemon = Pokemon.loadFromJSON(registryAccess,json.getAsJsonObject("pokemon"))
                PokemonPurchasingObj(pokemon)
            }catch (e : Throwable){
                e.printStackTrace()
                null
            }
        }
    }
}