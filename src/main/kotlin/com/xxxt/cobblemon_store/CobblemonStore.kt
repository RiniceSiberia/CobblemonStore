package com.xxxt.cobblemon_store

import com.mojang.logging.LogUtils
import com.xxxt.cobblemon_store.event.StoreEvents
import com.xxxt.cobblemon_store.store.StoresLibrary
import com.xxxt.cobblemon_store.utils.JsonFileUtils
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerStartingEvent

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CobblemonStore.Companion.MOD_ID)
class CobblemonStore(modEventBus: IEventBus, modContainer: ModContainer) {

    init {
        NeoForge.EVENT_BUS.register(this)
        StoresLibrary.register()
        Registrations.register(modEventBus)

        with(NeoForge.EVENT_BUS){
            addListener(Registrations::addBlockToTab)
            addListener(StoreEvents::onTooltipsEvent)
        }
    }

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent){
        JsonFileUtils.server = event.server
    }



    companion object {
        // Define mod id in a common place for everything to reference
        const val MOD_ID: String = "cobblemon_store"

        // Directly reference a slf4j logger
        val LOGGER: org.slf4j.Logger = LogUtils.getLogger()
    }
}