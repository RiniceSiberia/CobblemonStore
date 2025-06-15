package com.xxxt.cobblemon_store.store

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.party
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import com.xxxt.cobblemon_store.CobblemonStore
import com.xxxt.cobblemon_store.CobblemonStore.Companion.LOGGER
import com.xxxt.cobblemon_store.CobblemonStore.Companion.registryAccess
import com.xxxt.cobblemon_store.CobblemonStore.Companion.server
import com.xxxt.cobblemon_store.store.WarehouseLibrary.Warehouse
import com.xxxt.cobblemon_store.store.WarehouseLibrary.Warehouse.WarehouseItem
import com.xxxt.cobblemon_store.utils.JsonFileUtils
import com.xxxt.cobblemon_store.utils.PluginUtils
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.ItemHandlerHelper
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 玩家仓库
 */
object WarehouseLibrary : ConcurrentHashMap<UUID,Warehouse>(){

    init {
        load()
    }

    fun getOrCreate(player : Player) : Warehouse{
        if (this[player.uuid] == null)
            put(player.uuid, Warehouse(player.uuid))
        return this[player.uuid]!!
    }

    private fun readResolve(): Any = WarehouseLibrary

    fun load() : Boolean{
        return JsonFileUtils.loadWarehouseByJson()
    }

    fun save() : Boolean{
        return JsonFileUtils.saveWarehouseByJson()
    }

    fun register(){}

    class Warehouse(
        val playerUUID: UUID
    ) : ConcurrentHashMap<Int,WarehouseItem>(){
        val player get() = server.playerList.getPlayer(playerUUID)!!

        val nextEmptyIndex
            get() = let {
                var index = 0
                while (this[index] != null){
                    index++
                }
                index
            }

        fun serialize() : JsonObject{
            return JsonObject().apply apply1@{
                addProperty("player_uuid", playerUUID.toString())
                add("warehouse_items",JsonArray().apply apply2@{
                    this@Warehouse.forEach { k, v ->
                        this@apply2.add(JsonObject().apply apply3@{
                            this@apply3.addProperty("key",k)
                            this@apply3.add("value",v.serialize())
                        })
                    }
                })
            }
        }

        companion object{
            fun deserialize(json : JsonObject) : Warehouse{
                val playerUUID = UUID.fromString(json.get("player_uuid").asString)
                val warehouseItems = json.get("warehouse_items").asJsonArray
                val warehouse = Warehouse(playerUUID)
                warehouseItems.forEach {
                    val item = WarehouseItem.deserialize(it.asJsonObject)
                    if (item != null){
                        warehouse[item.index] = item
                    }
                }
                return warehouse
            }
        }


        /**
         * 仓库存储的单个项
         */
        sealed class WarehouseItem(
            val playerUUID: UUID,
            val index : Int
        ){
            val player get() = server.playerList.getPlayer(playerUUID)!!

            abstract val type : TradeType

            /**
             * 成交/取回
             */
            abstract fun retrieve()

            /**
             * 删除这个物品
             */
            fun removeIt(){
                WarehouseLibrary[playerUUID]?.apply {
                    this.remove(index)
                }
            }

            fun serialize(): JsonObject{
                return JsonObject().apply {
                    addProperty("name","warehouse_item")
                    addProperty("type",type.lowercaseName)
                    addProperty("player_uuid",playerUUID.toString())
                    addProperty("index",index.toString())
                    this.serialize()
                }
            }

            abstract fun JsonObject.serialize()

            companion object{
                fun deserialize(json : JsonObject): WarehouseItem?{
                    return try {
                        val name = json.get("name").asString
                        assert(name=="warehouse_item")
                        val type = json.get("type").asString
                        when(type){
                            TradeType.MONEY.lowercaseName -> MoneyWarehouseItem.deserialize(json)
                            TradeType.ITEM.lowercaseName -> ItemWarehouseItem.deserialize(json)
                            TradeType.POKEMON.lowercaseName -> PokemonWarehouseItem.deserialize(json)
                            else -> null
                        }
                    }catch (e : Throwable){
                        e.printStackTrace()
                        null
                    }
                }
            }

            class MoneyWarehouseItem(
                playerUUID: UUID,
                index : Int,
                val price : Double
            ): WarehouseItem(playerUUID,index){

                override val type: TradeType
                    get() = TradeType.MONEY

                override fun retrieve() {
                    PluginUtils.addMoney(player, price)
                    removeIt()
                }

                override fun JsonObject.serialize() {
                    addProperty("price",price)
                }
                companion object{
                    fun deserialize(json : JsonObject): MoneyWarehouseItem{
                        val playerUUID = UUID.fromString(json.get("player_uuid").asString)
                        val index = json.get("index").asInt
                        val price = json.get("price").asDouble
                        return MoneyWarehouseItem(playerUUID,index,price)
                    }
                }
            }

            class ItemWarehouseItem(
                playerUUID: UUID,
                index : Int,
                val stack : ItemStack
            ): WarehouseItem(playerUUID,index){
                override val type: TradeType
                    get() = TradeType.ITEM

                override fun retrieve() {
                    ItemHandlerHelper.giveItemToPlayer(player,stack)
                }

                override fun JsonObject.serialize() {
                    add("nbt",ItemStack.CODEC.encodeStart(JsonOps.INSTANCE,this@ItemWarehouseItem.stack)
                        .resultOrPartial{err ->
                            LOGGER.error("item stack序列化出错:${err}")
                        }.orElseThrow())
                }
                companion object{
                    fun deserialize(json : JsonObject): ItemWarehouseItem{
                        val playerUUID = UUID.fromString(json.get("player_uuid").asString)
                        val index = json.get("index").asInt
                        val stack = ItemStack.CODEC.parse(JsonOps.INSTANCE,json.getAsJsonObject("nbt"))
                            .resultOrPartial{err ->
                                LOGGER.error("item stack反序列化出错:${err}")
                            }.orElseThrow()
                        return ItemWarehouseItem(playerUUID,index,stack)
                    }
                }
            }

            class PokemonWarehouseItem(
                playerUUID: UUID,
                index : Int,
                val pokemon: Pokemon,
            ) : WarehouseItem(playerUUID,index){
                override val type: TradeType
                    get() = TradeType.POKEMON

                override fun retrieve() {
                    player.party().add(pokemon)
                    removeIt()
                }

                override fun JsonObject.serialize() {
                    add("pokemon",
                        pokemon.saveToJSON(registryAccess)
                        )
                }

                companion object{
                    fun deserialize(json : JsonObject): PokemonWarehouseItem{
                        val playerUUID = UUID.fromString(json.get("player_uuid").asString)
                        val index = json.get("index").asInt
                        val pokemon = Pokemon.loadFromJSON(
                            registryAccess,
                            json.getAsJsonObject("pokemon")
                        )
                        return PokemonWarehouseItem(playerUUID,index,pokemon)
                    }
                }
            }
        }
    }
}