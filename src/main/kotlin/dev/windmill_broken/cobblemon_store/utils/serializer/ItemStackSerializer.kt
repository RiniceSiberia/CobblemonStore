package dev.windmill_broken.cobblemon_store.utils.serializer

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.gsonConfig
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.kJsonConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import net.minecraft.world.item.ItemStack

object ItemStackSerializer : KSerializer<ItemStack> {
    override val descriptor = SerialDescriptor(
        "item_stack_nbt",
        JsonElement.serializer().descriptor
    )

    override fun serialize(encoder: Encoder, value: ItemStack) {
        val nbt = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE,value)
            .resultOrPartial{err ->
                CobblemonStore.Companion.LOGGER.error("item stack序列化出错:${err}")
            }.orElseThrow()
        val gsonStr = gsonConfig.toJson(nbt)
        val ktJson = kJsonConfig.decodeFromString(JsonElement.serializer(),gsonStr)
        (encoder as JsonEncoder).encodeJsonElement(ktJson)
    }

    override fun deserialize(decoder: Decoder): ItemStack {
        val nbt = (decoder as JsonDecoder).decodeJsonElement()
        val json = JsonParser.parseString(kJsonConfig.encodeToString(JsonElement.serializer(),nbt))
        val stack = ItemStack.CODEC.parse(JsonOps.INSTANCE, json)
            .resultOrPartial{err ->
                CobblemonStore.Companion.LOGGER.error("item stack反序列化出错:${err}")
            }.orElseThrow()
        return stack
    }
}