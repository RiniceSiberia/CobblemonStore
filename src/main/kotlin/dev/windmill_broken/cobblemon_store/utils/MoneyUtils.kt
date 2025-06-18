package dev.windmill_broken.cobblemon_store.utils

import net.impactdev.impactor.api.economy.EconomyService
import net.impactdev.impactor.api.economy.accounts.Account
import net.kyori.adventure.key.Key
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture

object MoneyUtils {
    /**
     * 获得玩家的钱
     * @param player 玩家的ID
     * @return 玩家的钱
     */
    fun selectMoney(player : ServerPlayer): BigDecimal{
        val future: CompletableFuture<Account?>
        = EconomyService.instance()
            .account(
                EconomyService
                    .instance()
                    .currencies()
                    .currency(
                        Key.key("point")
                    ).get(),
                player.getUUID())
        return future.thenApply { account ->
            return@thenApply account?.balance()?: BigDecimal.ZERO
        }.getNow(BigDecimal.ZERO)
    }

    fun setMoney(player: Player, value: BigDecimal) : BigDecimal{
        val future: CompletableFuture<Account?>
        = EconomyService.instance()
            .account(
                EconomyService
                    .instance()
                    .currencies()
                    .currency(
                        Key.key("point")
                    ).get(),
                player.getUUID())
        return future.thenApply { account ->
            account?.set(value)
            return@thenApply account?.balance()?: BigDecimal.ZERO
        }.getNow(BigDecimal.ZERO)
    }

    fun addMoney(player: Player, value: BigDecimal) : BigDecimal{
        val future: CompletableFuture<Account?>
        = EconomyService.instance()
            .account(
                EconomyService
                    .instance()
                    .currencies()
                    .currency(
                        Key.key("point")
                    ).get(),
                player.getUUID())
        return future.thenApply { account ->
            account?.deposit(value)
            return@thenApply account?.balance()?: BigDecimal.ZERO
        }.getNow(BigDecimal.ZERO)
    }

    fun minusMoney(player: Player, value: BigDecimal) : BigDecimal{
        val future: CompletableFuture<Account?>
        = EconomyService.instance()
            .account(
                EconomyService
                    .instance()
                    .currencies()
                    .currency(
                        Key.key("point")
                    ).get(),
                player.getUUID())
        return future.thenApply { account ->
            account?.withdraw(value)
            return@thenApply account?.balance()?: BigDecimal.ZERO
        }.getNow(BigDecimal.ZERO)
    }
}