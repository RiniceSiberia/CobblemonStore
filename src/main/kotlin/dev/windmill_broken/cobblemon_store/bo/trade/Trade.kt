package dev.windmill_broken.cobblemon_store.bo.trade

import com.google.gson.Gson
import dev.windmill_broken.cobblemon_store.Registrations
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.kJsonConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

@Serializable(with = TradeSerializer::class)
class Trade(
    val id : Int,
    val storeId : String,
    cost : CostObj,
    purchasing : PurchasingObj,
    storeLimits : Map<String,StoreLimit>
){
    val supStore
        get() = DAOWharf.storeLibrary[storeId]

    var cost : CostObj = cost
        private set(value) {
            saveChange()
            field = value
        }

    var purchasing: PurchasingObj = purchasing
        private set(value) {
            saveChange()
            field = value
        }

    val storeLimits : MutableMap<String, StoreLimit> = object : MutableMap<String, StoreLimit> {
        private val map = storeLimits.toMutableMap()

        override val keys: MutableSet<String>
            get() = map.keys
        override val values: MutableCollection<StoreLimit>
            get() = map.values
        override val entries: MutableSet<MutableMap.MutableEntry<String, StoreLimit>>
            get() = map.entries

        override fun put(
            key: String,
            value: StoreLimit
        ): StoreLimit? = map.put(key, value).also { saveChange() }

        override fun remove(key: String): StoreLimit? = map.remove(key).also { saveChange() }

        override fun putAll(from: Map<out String, StoreLimit>) = map.putAll(from).also { saveChange() }

        override fun clear() = map.clear().also { saveChange() }

        override val size: Int
            get() = map.size

        override fun isEmpty(): Boolean = map.isEmpty()

        override fun containsKey(key: String): Boolean = map.containsKey(key)

        override fun containsValue(value: StoreLimit): Boolean = map.containsValue(value)

        override fun get(key: String): StoreLimit? = map[key]
    }




    fun trade( player: Player) : Boolean{
        if (!cost.enough(player)){
            player.sendSystemMessage(Component.translatable("msg.cobblemon_store.not_enough_money"))
            return false
        }
        if (storeLimits.isNotEmpty() && !storeLimits.any { it.value.couldBuy(player) }){
            player.sendSystemMessage(Component.translatable("msg.cobblemon_store.sold_out"))
            return false
        }
        cost.pay(player).also { if(!it) return false }
        storeLimits.forEach { it.value.consume(player) }
        val warehouse = DAOWharf.warehouseLibrary.getOrCreate(player.uuid)
        val purchasingItem = purchasing.purchasing(player)
        warehouse.put(purchasingItem.index,purchasingItem)
        player.sendSystemMessage(
            purchasing.purchasingMsgComponent().also {
                it.append(cost.costMsgComponent())
            }
        )
        return true
    }

    fun saveChange(){
        DAOWharf.tradeLibrary.update(this)
    }

    fun removeIt(){
        DAOWharf.tradeLibrary.removeById(id)
    }

    val showedItemStack : ItemStack
        get(){
            if (purchasing is ItemPurchasingObj){
                return (purchasing as ItemPurchasingObj).stack.copy().also {
                    it[DataComponents.CUSTOM_NAME] = Component.translatable(
                        "item.cobblemon_store.sell_menu.slot.name",
                        Component.translatable("item.cobblemon_store.sell_menu.slot.sell"),
                        (purchasing as ItemPurchasingObj).stack.displayName
                    ).withStyle(ChatFormatting.GOLD)
                    it.set(
                        Registrations.TagTypes.TRADE_ITEM_TAG,
                        Gson().toJson(kJsonConfig.encodeToString(serializer(),this))
                    )
                }
            }else if (cost is ItemCostObj){
                return (cost as ItemCostObj).stack.copy().also {
                    it.set(DataComponents.CUSTOM_NAME,
                        Component.translatable(
                            "item.cobblemon_store.sell_menu.slot.name",
                            Component.translatable("item.cobblemon_store.sell_menu.slot.buy"),
                            Component.translatable(
                                "item.cobblemon_store.sell_menu.slot.money",
                                purchasing
                            )
                        )
                    )
                    it.set(
                        Registrations.TagTypes.TRADE_ITEM_TAG,
                        Gson().toJson(kJsonConfig.encodeToString(serializer(),this))
                    )
                }

            }
            return ItemStack.EMPTY
        }
}

object TradeSerializer : KSerializer<Trade> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("trade"){
        element("id",Int.serializer().descriptor)
        element("storeId",String.serializer().descriptor)
        element("cost",CostObj.serializer().descriptor)
        element("purchasing",PurchasingObj.serializer().descriptor)
        element("storeLimits",MapSerializer(String.serializer(),StoreLimit.serializer()).descriptor)
    }

    override fun serialize(
        encoder: Encoder,
        value: Trade
    ) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeIntElement(descriptor, 0, value.id)
        composite.encodeStringElement(descriptor, 1, value.storeId)
        composite.encodeSerializableElement(descriptor, 2, CostObj.serializer(), value.cost)
        composite.encodeSerializableElement(descriptor, 3, PurchasingObj.serializer(), value.purchasing)
        composite.encodeSerializableElement(descriptor, 4, MapSerializer(String.serializer(),StoreLimit.serializer()), value.storeLimits)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Trade {
        val dec = decoder.beginStructure(descriptor)
        var id = -1
        lateinit var storeId : String
        lateinit var cost : CostObj
        lateinit var purchasing : PurchasingObj
        val storeLimits = mutableMapOf<String, StoreLimit>()
        loop@ while (true) {
            when (val index = dec.decodeElementIndex(descriptor)) {
                0 -> id = dec.decodeIntElement(descriptor, index)
                1 -> storeId = dec.decodeStringElement(descriptor, index)
                2 -> cost = dec.decodeSerializableElement(descriptor, index, CostObj.serializer())
                3 -> purchasing = dec.decodeSerializableElement(descriptor, index, PurchasingObj.serializer())
                4 -> storeLimits.putAll(
                    dec.decodeSerializableElement(
                        descriptor,
                        index,
                        MapSerializer(String.serializer(), StoreLimit.serializer())
                    )
                )
                CompositeDecoder.DECODE_DONE -> break@loop
                else -> throw IllegalStateException()
            }
        }
        dec.endStructure(descriptor)
        assert(id > 0)
        return Trade(id,storeId,cost,purchasing,storeLimits)
    }
}