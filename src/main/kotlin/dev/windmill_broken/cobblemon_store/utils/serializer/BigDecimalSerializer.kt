package dev.windmill_broken.cobblemon_store.utils.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigDecimal

object BigDecimalSerializer : KSerializer<BigDecimal>{
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("big_decimal", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: BigDecimal) {
        if (encoder is JsonEncoder) {
            // 使用 JsonUnquotedLiteral 直接输出数字文本，不带引号
            val literal = JsonUnquotedLiteral(value.toPlainString())
            encoder.encodeJsonElement(literal)
        } else {
            // 非 JSON 格式下用字符串
            encoder.encodeString(value.toPlainString())
        }
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return if (decoder is JsonDecoder) {
            // 从 JSON 元素读取文本内容后转换为 BigDecimal
            val element = decoder.decodeJsonElement()
            element.jsonPrimitive.content.toBigDecimal()
        } else {
            // 其它格式从字符串解析
            decoder.decodeString().toBigDecimal()
        }
    }
}