package dev.hivens.hidemymods.forge1710;

import cpw.mods.fml.common.Mod;
import dev.hivens.hidemymods.common.ModMeta;

/**
 * Placeholder mod container. The 1.7.10 coremod + transformer that
 * matches the 1.12.2 implementation is not yet ported. The 1.7.10
 * handshake message lives at
 *   cpw.mods.fml.common.network.handshake.FMLHandshakeMessage$ModList
 * and the serialiser signature is the same as on 1.12.2, so the
 * port should mainly be a rename of the target class string and a
 * rebuild against the older Forge classpath.
 */
@Mod(
    modid                    = ModMeta.MOD_ID,
    name                     = ModMeta.MOD_NAME,
    version                  = "0.1.0",
    acceptableRemoteVersions = "*"
)
public final class HideMyMods {
}
