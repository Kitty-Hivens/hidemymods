package dev.hivens.hidemymods.neoforge1211;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * Placeholder mod container. NeoForge replaced the legacy FML
 * handshake with the configuration-phase custom-payload system in
 * 1.20.2, so the 1.12.2 strategy of patching FMLHandshakeMessage
 * does not transfer. The target hook on 1.21.1 is the mod-list
 * payload registered by NeoForge during the configuration phase
 * (see ModListPayload / channel-comparison handler in
 * net.neoforged.neoforge.network); the exact class to transform
 * needs verification against the 21.1.144 source before any
 * coremod work begins. ModDevGradle's runtime exposes the source,
 * so the verification step is local-only once the toolchain is
 * resolved.
 */
@Mod("hidemymods")
public final class HideMyMods {
    public HideMyMods(IEventBus modEventBus, ModContainer modContainer) {
    }
}
