package dev.hivens.hidemymods.forge1710;

import cpw.mods.fml.common.Mod;
import dev.hivens.hidemymods.common.ModMeta;

/**
 * Mod container. The work is done by the coremod the jar manifest
 * names; this exists so the mod appears in the list and declares a
 * version, and so FML has something to attach the id to.
 *
 * `acceptableRemoteVersions = "*"` because a server running this has
 * no reason to care which build a client carries, and the point of the
 * mod is that the reported list is the operator's to decide.
 */
@Mod(
    modid                    = ModMeta.MOD_ID,
    name                     = ModMeta.MOD_NAME,
    version                  = "0.1.0",
    acceptableRemoteVersions = "*"
)
public final class HideMyMods {
}
