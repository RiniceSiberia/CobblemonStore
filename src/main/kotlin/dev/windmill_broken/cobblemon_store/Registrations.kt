package dev.windmill_broken.cobblemon_store

import com.mojang.serialization.Codec
import dev.windmill_broken.cobblemon_store.CobblemonStore.Companion.MOD_ID
import dev.windmill_broken.cobblemon_store.block.StoreBlock
import dev.windmill_broken.cobblemon_store.block.StoreBlockEntity
import dev.windmill_broken.cobblemon_store.menu.StoreMenuSupplier
import dev.windmill_broken.cobblemon_store.net.NetworkHandler
import dev.windmill_broken.cobblemon_store.net.StoreConfigPacket
import dev.windmill_broken.cobblemon_store.screen.StoreScreen
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.PayloadRegistrar
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier


object Registrations {

    object StoreItems {
        val ITEMS = DeferredRegister.createItems(MOD_ID)

        fun register(eventBus: IEventBus) {
            ITEMS.register(eventBus)
        }
    }

    object StoreBlocks {
        val BLOCKS = DeferredRegister.createBlocks(MOD_ID)

        val STORE_BLOCK = registerBlockItems("store_block") { StoreBlock(BlockBehaviour.Properties.of()) }

        fun register(eventBus: IEventBus) {
            BLOCKS.register(eventBus)
        }

        private fun <T : Block> registerBlockItems(
            name: String,
            blockFactory: Supplier<T>
        ): DeferredHolder<Block, T> {
            val blockItem = BLOCKS.register(name, blockFactory)
            registerBlockItem(name, blockItem)
            return blockItem
        }

        private fun <T : Block> registerBlockItem(
            name: String,
            block: DeferredBlock<T>
        ): DeferredHolder<Item, Item> {
            return StoreItems.ITEMS.registerItem(name) {
                BlockItem(
                    block.get(),
                    Item.Properties()
                )
            }
        }
    }


    object MenuTypes {
        val MENU_TYPES: DeferredRegister<MenuType<*>> =
            DeferredRegister
                .create(Registries.MENU, MOD_ID)
        val STORE_MENU = MENU_TYPES.register(
            "store_menu",
            Supplier {
                MenuType(
                    StoreMenuSupplier,
                    FeatureFlags.DEFAULT_FLAGS
                )
            })

        fun register(eventBus: IEventBus) {
            MENU_TYPES.register(eventBus)
        }

    }

    object ScreenTypes {
        fun onRegisterScreen(event: RegisterMenuScreensEvent) {
            event.register(MenuTypes.STORE_MENU.get()) { menu, inventory, title ->
                StoreScreen(
                    menu,
                    inventory,
                    title
                )
            }
        }
    }

    object NetworkTypes {
        fun registerNetworking(event: RegisterPayloadHandlersEvent) {
            val registrar: PayloadRegistrar = event.registrar("1.0.0")

            registrar.playToServer(
                StoreConfigPacket.TYPE,
                StoreConfigPacket.STREAM_CODEC
            ) { packet, context ->
                NetworkHandler.handleStoreConfigChanged(packet, context)
            }
        }
    }

    object TagTypes {
        val TAGS = DeferredRegister.createDataComponents(
            Registries.DATA_COMPONENT_TYPE,
            "tags"
        )

        val TRADE_ITEM_TAG: DeferredHolder<DataComponentType<*>, DataComponentType<String>> =
            TAGS.registerComponentType("trade_item_tag") {
                DataComponentType.builder<String>()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            }

        fun register(eventBus: IEventBus) {
            TAGS.register(eventBus)
        }
    }

    object BlockEntities {
        val BLOCK_ENTITIES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE,
            MOD_ID
        )
        val STORE_BLOCK_ENTITY_TYPE = BLOCK_ENTITIES.register(
            "store_block_entity_type",
            Supplier {
                BlockEntityType.Builder.of(
                    ::StoreBlockEntity,
                    StoreBlocks.STORE_BLOCK.get()
                ).build(null)
            }
        )

        fun register(eventBus: IEventBus) {
            BLOCK_ENTITIES.register(eventBus)
        }
    }


    fun register(eventBus: IEventBus) {
        StoreItems.register(eventBus)
        StoreBlocks.register(eventBus)
        MenuTypes.register(eventBus)
        TagTypes.register(eventBus)
        BlockEntities.register(eventBus)
    }

    fun addBlockToTab(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey == CreativeModeTabs.REDSTONE_BLOCKS) {
            // StoreBlocks.ALL NOT WORKING, use entries instead
            StoreBlocks.BLOCKS.entries.forEach {
                event.accept(it.get())
            }
        }
    }
}