package dev.hivens.hidemymods.forge112.coremod;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/**
 * Entry point referenced by the jar manifest's FMLCorePlugin
 * attribute. FML resolves this class very early -- before any @Mod
 * class is constructed -- and registers the returned transformers
 * with LaunchWrapper so they apply to subsequent class loads.
 *
 * TransformerExclusions keeps the coremod's own classes off the
 * transformation path. Without it, a transformer that references
 * other coremod classes can deadlock on class loading because the
 * transformation pipeline re-enters itself.
 */
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("hidemymods coremod")
@IFMLLoadingPlugin.TransformerExclusions("dev.hivens.hidemymods.forge112.coremod")
@IFMLLoadingPlugin.SortingIndex(1001)
public final class HideMyModsCorePlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
            "dev.hivens.hidemymods.forge112.coremod.ModListTransformer"
        };
    }

    @Override public String getModContainerClass()        { return null; }
    @Override public String getSetupClass()               { return null; }
    @Override public void   injectData(Map<String, Object> data) { }
    @Override public String getAccessTransformerClass()   { return null; }
}
