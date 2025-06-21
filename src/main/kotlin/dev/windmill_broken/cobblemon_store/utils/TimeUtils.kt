package dev.windmill_broken.cobblemon_store.utils

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    fun getExpiredText(
        expiredTime : Calendar,
        tooltipPath : String,
        expiredPath : String
    ) : MutableComponent {
        val diffInMillis = expiredTime.timeInMillis - Calendar.getInstance().timeInMillis
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        if (diffInMillis > 0){ // 将毫秒差值转换为秒、分钟、小时、天数
            val seconds = (diffInMillis / 1000) % 60
            val secondsText = if (seconds > 0){
                Component.translatable("unit.cobblemon_store.time.second",seconds)
            }else{
                Component.empty()
            }
            val minutes = (diffInMillis / (1000 * 60)) % 60
            val minutesText = if (minutes > 0) {
                Component.translatable("unit.cobblemon_store.time.minute",minutes)
            } else{
                Component.empty()
            }
            val hours = (diffInMillis / (1000 * 60 * 60)) % 24
            val hoursText = if (hours > 0) {
                Component.translatable("unit.cobblemon_store.time.hour",hours)
            } else{
                Component.empty()
            }
            val days = (diffInMillis / (1000 * 60 * 60 * 24)) % 7
            val daysText = if (days > 0) {
                Component.translatable("unit.cobblemon_store.time.day",days)
            } else{
                Component.empty()
            }
            val weeks = diffInMillis / (1000 * 60 * 60 * 24 * 7)
            val weeksText = if (weeks > 0) {
                Component.translatable("unit.cobblemon_store.time.week",weeks)
            } else{
                Component.empty()
            }
            return Component.translatable(tooltipPath, formatter.format(expiredTime.time),
                weeksText
                    .append(daysText)
                    .append(hoursText)
                    .append(minutesText)
                    .append(secondsText)
                    .withStyle(ChatFormatting.ITALIC)
                    .also { it ->
                        if (days < 1){
                            it.withStyle(ChatFormatting.RED)
                        }
                    }
            )
        }else{
            return Component.translatable(
                expiredPath,
                formatter.format(expiredTime.time)
            ).withStyle(ChatFormatting.RED)
                .withStyle(ChatFormatting.ITALIC)
        }
    }
}