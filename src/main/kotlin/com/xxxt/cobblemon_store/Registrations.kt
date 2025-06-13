package com.xxxt.cobblemon_store

import com.mojang.serialization.Codec
import com.xxxt.cobblemon_store.CobblemonStore.Companion.MOD_ID
import com.xxxt.cobblemon_store.block.StoreBlock
import com.xxxt.cobblemon_store.menu.StoreMenu
import com.xxxt.cobblemon_store.menu.StoreMenuSupplier
import com.xxxt.cobblemon_store.screen.StoreScreen
import com.xxxt.cobblemon_store.store.Store
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.CreativeModeTabs
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.network.IContainerFactory
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object Registrations {

    object StoreBlocks{
        val BLOCKS = DeferredRegister.createBlocks(MOD_ID)

        val STORE_BLOCK = BLOCKS.register("store_block", Supplier{ StoreBlock() })

        val ALL = listOf(
            STORE_BLOCK,
        )

        fun register(eventBus: IEventBus){
            BLOCKS.register(eventBus)
        }
    }


    object MenuTypes{
        val MENU_TYPES: DeferredRegister<MenuType<*>> =
            DeferredRegister
                .create(Registries.MENU,MOD_ID)
        val STORE_MENU = MENU_TYPES.register(
            "store_menu",
            Supplier{
                MenuType(
                    StoreMenuSupplier,
                    FeatureFlags.DEFAULT_FLAGS
            )
        })


        val ALL = listOf(
            STORE_MENU
        )

        fun register(eventBus: IEventBus){
            MENU_TYPES.register(eventBus)
        }

    }

    object TagTypes{
        val TAGS = DeferredRegister.createDataComponents(
            Registries.DATA_COMPONENT_TYPE,
            "tags"
        )

        val TRADE_ITEM_TAG
        = TAGS.registerComponentType("trade_item_tag"){
            DataComponentType.builder<String>()
                .persistent(Codec.STRING)
                .networkSynchronized(ByteBufCodecs.STRING_UTF8)
        }

        fun register(eventBus: IEventBus) {
            TAGS.register(eventBus)
        }
    }




    fun register(eventBus: IEventBus){
        StoreBlocks.register(eventBus)
        MenuTypes.register(eventBus)
        TagTypes.register(eventBus)
    }

    fun addBlockToTab(event: BuildCreativeModeTabContentsEvent){
        if (event.tabKey == CreativeModeTabs.REDSTONE_BLOCKS){
            StoreBlocks.ALL.forEach {
                event.accept(it)
            }
        }
    }
}