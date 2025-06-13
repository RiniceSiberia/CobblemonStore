package com.xxxt.cobblemon_store.utils

import com.mojang.logging.LogUtils
import com.mojang.serialization.JsonOps
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import java.util.*

val LOGGER: org.slf4j.Logger = LogUtils.getLogger()


object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("uuid", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeStringElement(descriptor,0,value.toString())
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): UUID {
        val composite = decoder.beginStructure(descriptor)
        val uuid : String = composite.decodeStringElement(descriptor,0)
        composite.endStructure(descriptor)
        return UUID.fromString(uuid)
    }
}

object ResourceLocationSerializer: KSerializer<ResourceLocation>{
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("resource_location", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ResourceLocation) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeStringElement(descriptor,0,value.toString())
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): ResourceLocation {
        val composite = decoder.beginStructure(UUIDSerializer.descriptor)
        val location : String = composite.decodeStringElement(descriptor,0)
        composite.endStructure(descriptor)
        return ResourceLocation.parse(location)
    }

}

object ItemStackSerializer : KSerializer<ItemStack>{
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("item_stack", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ItemStack) {
        val composite = encoder.beginStructure(descriptor)
        val nbt = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE,value)
            .resultOrPartial{err ->
                LOGGER.error("item stack序列化出错:${err}")
            }.orElseThrow()
        composite.encodeStringElement(descriptor,0,nbt.toString())
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): ItemStack {
        val composite = decoder.beginStructure(descriptor)
        val nbt = composite.decodeStringElement(descriptor,0).let {
            com.google.gson.JsonParser.parseString(it)
        }
        composite.endStructure(descriptor)
        return ItemStack.CODEC.parse(JsonOps.INSTANCE,nbt)
            .resultOrPartial{err ->
                LOGGER.error("item stack反序列化出错:${err}")
            }.orElseThrow()
    }
}

