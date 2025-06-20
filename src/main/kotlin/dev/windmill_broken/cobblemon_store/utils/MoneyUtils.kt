package dev.windmill_broken.cobblemon_store.utils

import net.impactdev.impactor.api.economy.EconomyService
import net.impactdev.impactor.api.economy.accounts.Account
import net.impactdev.impactor.api.economy.currency.Currency
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture

object MoneyUtils {

    val currencyTypes
        get() = EconomyService
            .instance()
            .currencies()
            .registered()
            .associateBy { c -> c.key().value() }

    val primaryCurrency
        get() = EconomyService
            .instance()
            .currencies()
            .primary()

    fun getCurrencyType(key: String): Currency? {
        return currencyTypes[key]
    }

    fun String.withCurrencySign(key: String): String {
        return (getCurrencyType(key)?.symbol()
            ?.let { toString() + " " }
            ?: "") + this
    }

    /**
     * 获得玩家的钱
     * @param player 玩家的ID
     * @return 玩家的钱
     */
    fun getCurrency(
        player: ServerPlayer,
        currencyKey: String = primaryCurrency.key().value()
    ): BigDecimal {
        val future: CompletableFuture<Account?> = EconomyService.instance()
            .account(
                getCurrencyType(currencyKey),
                player.uuid
            )
        return future.thenApply { account ->
            return@thenApply account?.balance() ?: BigDecimal.ZERO
        }.getNow(BigDecimal.ZERO)
    }

    fun setMoney(
        player: Player,
        value: BigDecimal,
        currencyKey: String = primaryCurrency.key().value()
    ): BigDecimal {
        val future: CompletableFuture<Account?> = EconomyService.instance()
            .account(
                getCurrencyType(currencyKey),
                player.uuid
            )
        return future.thenApply { account ->
            account?.set(value)
            return@thenApply account?.balance() ?: BigDecimal.ZERO
        }.getNow(BigDecimal.ZERO)
    }

    fun addMoney(
        player: Player,
        value: BigDecimal,
        currencyKey: String = primaryCurrency.key().value()
    ): BigDecimal {
        val future: CompletableFuture<Account?> = EconomyService.instance()
            .account(
                getCurrencyType(currencyKey),
                player.uuid
            )
        return future.thenApply { account ->
            account?.deposit(value)
            return@thenApply account?.balance() ?: BigDecimal.ZERO
        }.getNow(BigDecimal.ZERO)
    }

    fun minusMoney(
        player: Player,
        value: BigDecimal,
        currencyKey: String = primaryCurrency.key().value()
    ): BigDecimal {
        val future: CompletableFuture<Account?> = EconomyService.instance()
            .account(
                getCurrencyType(currencyKey),
                player.uuid
            )
        return future.thenApply { account ->
            account?.withdraw(value)
            return@thenApply account?.balance() ?: BigDecimal.ZERO
        }.getNow(BigDecimal.ZERO)
    }
}