package dev.windmill_broken.cobblemon_store

import com.mojang.logging.LogUtils
import dev.windmill_broken.cobblemon_store.dao.DAOWharf
import dev.windmill_broken.cobblemon_store.event.StoreEvents
import dev.windmill_broken.cobblemon_store.utils.TestObj
import net.minecraft.core.RegistryAccess
import net.minecraft.server.MinecraftServer
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import org.slf4j.Logger

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CobblemonStore.Companion.MOD_ID)
class CobblemonStore(modEventBus: IEventBus, modContainer: ModContainer) {

    init {
        NeoForge.EVENT_BUS.register(this)
        Registrations.register(modEventBus)

        with(modEventBus){
            addListener(Registrations::addBlockToTab)
            addListener(Registrations.ScreenTypes::onRegisterScreen)
            addListener(Registrations.NetworkTypes::registerNetworking)
        }
        with(NeoForge.EVENT_BUS){
            addListener(StoreEvents::onTooltipsEvent)
        }

        TestObj.test()

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.spec)
    }

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent){
        server = event.server
        registryAccess = server.allLevels.first().registryAccess()
    }

    @SubscribeEvent
    fun onServerTicket(event : ServerTickEvent.Post){
        tickCounter++

        if (tickCounter >= TICKS_PER_30_MINUTES) {
            tickCounter = 0
            DAOWharf.save()
        }
    }

    @SubscribeEvent
    fun onServerStopping(event: ServerStoppingEvent){
        DAOWharf.save()
    }



    companion object {
        lateinit var server: MinecraftServer
        // Define mod id in a common place for everything to reference

        lateinit var registryAccess: RegistryAccess

        const val MOD_ID: String = "cobblemon_store"

        private var tickCounter = 0

        const val TICKS_PER_30_MINUTES = 30 * 60 * 20  // 36000 ticks

        // Directly reference a slf4j logger
        val LOGGER: Logger = LogUtils.getLogger()
    }
}