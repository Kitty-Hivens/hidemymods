package dev.hivens.hidemymods.forge112;

import dev.hivens.hidemymods.common.ModMeta;
import net.minecraftforge.fml.common.Mod;

/**
 * The mod container is intentionally empty. All behaviour lives in
 * the coremod transformer which runs before any @Mod class loads.
 * The container exists so that the user can confirm the mod is
 * present in the in-game mod list and so that the jar passes the
 * FMLCorePluginContainsFMLMod scan.
 *
 * clientSideOnly = true because the mod has zero effect when loaded
 * by a dedicated server: the only thing it touches is the outbound
 * handshake on the client side.
 */
@Mod(
    modid            = ModMeta.MOD_ID,
    name             = ModMeta.MOD_NAME,
    version          = "0.1.0",
    acceptableRemoteVersions = "*",
    clientSideOnly   = true
)
public final class HideMyMods {
}
