package dev.windmill_broken.cobblemon_store.utils.serializer

import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.JsonParser
import dev.windmill_broken.cobblemon_store.CobblemonStore
import dev.windmill_broken.cobblemon_store.utils.JsonFileUtils.gsonConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PokemonSerializer : KSerializer<Pokemon> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "pokemon_nbt",
        PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: Pokemon
    ) {
        val json = value.saveToJSON(CobblemonStore.registryAccess)
        val str = gsonConfig.toJson(json)
        encoder.encodeString(str)
    }

    override fun deserialize(decoder: Decoder): Pokemon {
        val str = decoder.decodeString()
        val json = JsonParser.parseString(str).asJsonObject
        val pokemon = Pokemon.loadFromJSON(
            CobblemonStore.Companion.registryAccess,
            json
        )
        return pokemon
    }
}