@file:UseSerializers(
    UUIDSerializer::class
)
package dev.windmill_broken.cobblemon_store.bo.warehouse

import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.utils.serializer.UUIDSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*
import java.util.concurrent.ConcurrentHashMap


@Serializable(with = WarehouseSerializer::class)
class Warehouse(
    val playerUUID: UUID
) : ConcurrentHashMap<Int, WarehouseItem>(){
    val player get() = CobblemonStore.Companion.server.playerList.getPlayer(playerUUID)!!
    val nextEmptyIndex
        get() = let {
            var index = 0
            while (this[index] != null){
                index++
            }
            index
        }
}

object WarehouseSerializer : KSerializer<Warehouse>{

    private val uuidSerializer = UUIDSerializer
    val mapSerializer = MapSerializer(
        Int.serializer(),
        WarehouseItem.serializer()
    )

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("warehouse") {
            element("player_uuid", uuidSerializer.descriptor)
            element("map", mapSerializer.descriptor)
        }

    override fun serialize(
        encoder: Encoder,
        value: Warehouse
    ) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(descriptor, 0, uuidSerializer, value.playerUUID)
        composite.encodeSerializableElement(descriptor, 1, mapSerializer, value.toMap())
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Warehouse {
        val dec = decoder.beginStructure(descriptor)
        lateinit var pid: UUID
        lateinit var map: Map<Int, WarehouseItem>
        loop@ while (true) {
            when (val idx = dec.decodeElementIndex(descriptor)) {
                0 -> pid = dec.decodeSerializableElement(descriptor, 0, uuidSerializer)
                1 -> map = dec.decodeSerializableElement(descriptor, 1, mapSerializer)
                CompositeDecoder.DECODE_DONE -> break@loop
                else -> error("Unexpected index $idx")
            }
        }
        dec.endStructure(descriptor)
        val ware = Warehouse(pid)
        for ((k, v) in map) ware[k] = v
        return ware
    }
}