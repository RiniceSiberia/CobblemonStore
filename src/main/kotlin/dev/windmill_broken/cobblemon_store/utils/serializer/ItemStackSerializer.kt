package dev.windmill_broken.cobblemon_store.utils.serializer

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.gsonConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.world.item.ItemStack

object ItemStackSerializer  : KSerializer<ItemStack> {
    override val descriptor = PrimitiveSerialDescriptor(
        "item_stack_nbt",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: ItemStack) {
        val nbt = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE,value)
            .resultOrPartial{err ->
                CobblemonStore.Companion.LOGGER.error("item stack序列化出错:${err}")
            }.orElseThrow()
        encoder.encodeString(gsonConfig.toJson(nbt))
    }

    override fun deserialize(decoder: Decoder): ItemStack {
        val nbt = decoder.decodeString()
        val json = JsonParser.parseString(nbt)
        val stack = ItemStack.CODEC.parse(JsonOps.INSTANCE, json)
            .resultOrPartial{err ->
                CobblemonStore.Companion.LOGGER.error("item stack反序列化出错:${err}")
            }.orElseThrow()
        return stack
    }
}