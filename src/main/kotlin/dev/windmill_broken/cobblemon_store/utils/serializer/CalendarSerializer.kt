package dev.windmill_broken.cobblemon_store.utils.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

/**
 *      使用IDEA编写
 * @Author: DUELIST
 * @Time:  2023-08-27-14:25
 * @Message: 日期类序列化
 **/
object CalendarSerializer : KSerializer<Calendar> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Calendar", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Calendar) {
        val formattedDate = value.timeInMillis.toString()
        encoder.encodeString(formattedDate)
    }

    override fun deserialize(decoder: Decoder): Calendar {
        val formattedDate = decoder.decodeString()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = formattedDate.toLong()
        return calendar
    }
}