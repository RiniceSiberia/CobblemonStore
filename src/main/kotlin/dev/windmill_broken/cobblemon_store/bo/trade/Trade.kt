package dev.windmill_broken.cobblemon_store.bo.trade

import com.cobblemon.mod.common.CobblemonItems
import dev.windmill_broken.cobblemon_store.Registrations
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.kJsonConfig
import dev.windmill_broken.cobblemon_store.utils.MoneyUtils
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

@Serializable(with = TradeSerializer::class)
class Trade(
    val id : Int,
    @SerialName("store_id")
    val storeId : String,
    val creator: TradeCreator = ServerTradeCreator,
    @SerialName("auto_remove")
    val autoRemove : Boolean = false,
    cost : Cost,
    purchasing : Purchasing,
    storeLimits : Set<StoreLimit>
){
    val supStore
        get() = DAOWharf.storeLibrary[storeId]

    var cost : Cost = cost
        private set(value) {
            saveChange()
            field = value
        }

    var purchasing: Purchasing = purchasing
        private set(value) {
            saveChange()
            field = value
        }

    val storeLimits : MutableSet<StoreLimit> = object : MutableSet<StoreLimit> {
        val set = storeLimits.toMutableSet()

        override fun add(element: StoreLimit): Boolean {
            val clazz = element::class
            if (clazz in set.map { it::class }) return false
            return set.add(element).also { saveChange() }
        }

        override fun addAll(elements: Collection<StoreLimit>): Boolean {
            val clazz = elements.map { it::class }
            if (clazz.size != clazz.toSet().size) return false
            if ((clazz intersect set.map { it::class }).isNotEmpty()) return false
            return set.addAll(elements).also {
                saveChange()
            }
        }

        override fun clear() = set.clear().also { saveChange() }

        override fun iterator(): MutableIterator<StoreLimit> {
            return set.iterator()
        }

        override fun remove(element: StoreLimit): Boolean {
            return set.remove(element).also { saveChange() }
        }

        override fun removeAll(elements: Collection<StoreLimit>): Boolean {
            return set.removeAll(elements).also { saveChange() }
        }

        override fun retainAll(elements: Collection<StoreLimit>): Boolean {
            return set.retainAll(elements).also { saveChange() }
        }

        override val size: Int
            get() = set.size

        override fun isEmpty(): Boolean =set.isEmpty()

        override fun contains(element: StoreLimit): Boolean = set.contains(element)

        override fun containsAll(elements: Collection<StoreLimit>): Boolean = set.containsAll(elements)
    }




    fun trade( player: Player) : Boolean{
        if (player is ServerPlayer){
            if (!cost.enough(player)){
                player.sendSystemMessage(Component.translatable("msg.cobblemon_store.not_enough_money"))
                return false
            }
            if (storeLimits.isNotEmpty() && !storeLimits.any { it.couldBuy(player) }){
                player.sendSystemMessage(Component.translatable("msg.cobblemon_store.sold_out"))
                return false
            }
            cost.pay(player).also { if(!it) return false }
            storeLimits.forEach { it.consume(player,this) }
            val warehouse = DAOWharf.warehouseLibrary.getOrCreate(player.uuid)
            val purchasingItem = purchasing.purchasing(player,this)
            warehouse.put(warehouse.nextEmptyIndex,purchasingItem)
            if (warehouse.automaticAcquisition
                && ((purchasingItem.creator is ServerTradeCreator)
                        ||(purchasingItem.creator is PlayerTradeCreator
                        && (purchasingItem.creator as PlayerTradeCreator).playerId == player.uuid))){
                purchasingItem.retrieve(warehouse)
            }
            player.sendSystemMessage(
                purchasing.purchasingMsgComponent().also {
                    val msg = cost.costMsgComponent()
                    if (msg != Component.empty()){
                        it.append(msg)
                    }
                }
            )
            if (autoRemove && storeLimits.isNotEmpty() && !storeLimits.all { it.couldBuy(player) }){
                removeIt()
            }
            return true
        }
        return false
    }

    fun saveChange(){
        DAOWharf.tradeLibrary.update(this)
    }

    fun removeIt(){
        DAOWharf.tradeLibrary.removeById(id)
    }

    val showedItemStack : ItemStack
        get(){
            val purchasingIsMoney = purchasing is MoneyPurchasing
            val costIsMoney = cost is MoneyCost
            val typeComponent = Component.translatable(
                "item.cobblemon_store.sell_menu.slot." +
                        if (purchasingIsMoney){
                            if (costIsMoney){
                                "financial"
                            }else{
                                "buy"
                            }
                        }else{
                            if (costIsMoney){
                                "sell"
                            }else{
                                "trade"
                            }
                        }
            ).withStyle(ChatFormatting.BOLD).apply {
                if (purchasingIsMoney){
                    if (costIsMoney){
                        withStyle(ChatFormatting.GOLD)
                    }else{
                        withStyle(ChatFormatting.AQUA)
                    }
                }else{
                    if (costIsMoney){
                        withStyle(ChatFormatting.GREEN)
                    }else{
                        withStyle(ChatFormatting.WHITE)
                    }
                }
            }

            val stack = when (purchasing) {
                is ItemStackPurchasing -> {
                    (purchasing as ItemStackPurchasing).stack.copy()
                }
                is PokemonPurchasing -> {
                    ItemStack(CobblemonItems.POKE_BALL)
                }
                is MoneyPurchasing -> {
                    when(cost){
                        is SimpleItemCost -> {
                            (cost as SimpleItemCost).tempStack?: ItemStack(Items.BARRIER)
                        }
                        is MoneyCost -> {
                            ItemStack(Items.EMERALD)
                        }
                        is FreeCost -> {
                            ItemStack(Items.EMERALD)
                        }
                    }
                }
            }
            val originName = when (purchasing) {
                is ItemStackPurchasing -> {
                    //买物品直接给
                    (purchasing as ItemStackPurchasing).stack.let {
                        Component.empty().append(it.hoverName).append("x").append(it.count.toString())
                    }
                }
                is PokemonPurchasing -> {
                    (purchasing as PokemonPurchasing).pokemon.getDisplayName()
                }
                is MoneyPurchasing -> {
                    val purchasingTypSign = MoneyUtils.getCurrencySignal((purchasing as MoneyPurchasing).currencyType)
                    //追求的是money,分情况
                    when(cost){
                        is SimpleItemCost -> {
                            ((cost as SimpleItemCost).tempStack?: ItemStack(Items.BARRIER)).let {
                                Component.empty().append(it.hoverName).append("x").append(it.count.toString())
                            }
                        }
                        is MoneyCost -> {
                            val costTypeSign = MoneyUtils.getCurrencySignal((cost as MoneyCost).currencyType)
                            Component.literal(
                                "use"+costTypeSign +
                                        (cost as MoneyCost).value+
                                        "to exchange " +
                                        purchasingTypSign +
                                        (purchasing as MoneyPurchasing).value
                            )
                        }
                        is FreeCost -> {
                            Component.literal("FREE GET ${purchasingTypSign + (purchasing as MoneyPurchasing).value}!")
                        }
                    }
                }
            }
            stack[DataComponents.CUSTOM_NAME] =
                typeComponent.append(originName)

            stack.set(
                Registrations.TagTypes.TRADE_ITEM_TAG,
                kJsonConfig.encodeToString(this)
            )
            return stack
        }
}

object TradeSerializer : KSerializer<Trade> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("trade"){
        element("id",Int.serializer().descriptor)
        element("store_id",String.serializer().descriptor)
        element("trade_creator",TradeCreator.serializer().descriptor)
        element("auto_remove", Boolean.serializer().descriptor)
        element("cost",Cost.serializer().descriptor)
        element("purchasing",Purchasing.serializer().descriptor)
        element("store_limits",SetSerializer(StoreLimit.serializer()).descriptor)
    }

    override fun serialize(
        encoder: Encoder,
        value: Trade
    ) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeIntElement(descriptor, 0, value.id)
        composite.encodeStringElement(descriptor, 1, value.storeId)
        composite.encodeSerializableElement(descriptor,2,
            TradeCreator.serializer(),
            value.creator
        )
        composite.encodeSerializableElement(descriptor, 4,
            Cost.serializer(),
            value.cost
        )
        composite.encodeSerializableElement(descriptor, 5,
            Purchasing.serializer(),
            value.purchasing)

        composite.encodeSerializableElement(descriptor, 6,
            SetSerializer(StoreLimit.serializer()),
            value.storeLimits.toSet()
        )
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Trade {
        val dec = decoder.beginStructure(descriptor)
        var id = -1
        lateinit var storeId : String
        lateinit var creator : TradeCreator
        var autoRemove = false
        lateinit var cost : Cost
        lateinit var purchasing : Purchasing
        val storeLimits = mutableSetOf<StoreLimit>()
        loop@ while (true) {
            when (val index = dec.decodeElementIndex(descriptor)) {
                0 -> id = dec.decodeIntElement(descriptor, index)
                1 -> storeId = dec.decodeStringElement(descriptor, index)
                2 -> creator = dec.decodeSerializableElement(descriptor, index, TradeCreator.serializer())
                3 -> autoRemove = dec.decodeBooleanElement(descriptor, index)
                4 -> cost = dec.decodeSerializableElement(descriptor, index, Cost.serializer())
                5 -> purchasing = dec.decodeSerializableElement(descriptor, index, Purchasing.serializer())
                6 -> storeLimits.addAll(
                    dec.decodeSerializableElement(
                        descriptor,
                        index,
                        SetSerializer( StoreLimit.serializer())
                    )
                )
                CompositeDecoder.DECODE_DONE -> break@loop
                else -> throw IllegalStateException()
            }
        }
        dec.endStructure(descriptor)
        assert(id >= 0)
        return Trade(id,storeId,creator,autoRemove,cost,purchasing,storeLimits)
    }
}