package dev.hivens.hidemymods.forge112.coremod;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime helper invoked from the rewritten body of
 * FMLHandshakeMessage$ModList.toBytes. Lives in the coremod package so
 * the IFMLLoadingPlugin TransformerExclusions covers it -- nothing
 * here may be transformed by the same plugin that depends on it.
 *
 * Reads a JSON config file each time toBytes runs. Handshakes happen
 * once per connection so the cost is negligible and the user can
 * tweak the file without restarting Minecraft. Missing or invalid
 * config means passthrough: the real loaded mod-list goes on the
 * wire and the mod becomes a silent no-op. Failing closed (sending
 * an empty list) would re-create the v1 bug.
 *
 * Config path resolution:
 *   1. system property `hidemymods.spoof.file`
 *   2. `hidemymods-spoof.json` in the JVM working directory
 *
 * Config format:
 *   {
 *     "mods": [
 *       {"id": "appliedenergistics2", "version": "rv6-stable-7"},
 *       {"id": "buildcraftlib",       "version": "7.99.24.6"}
 *     ]
 *   }
 *
 * Unknown JSON fields are tolerated (Gson default). Entries with a
 * missing id or version are skipped silently to keep the wire payload
 * well-formed.
 */
public final class ModListSpoof {

    private static final String SYS_PROP   = "hidemymods.spoof.file";
    private static final String DEFAULT_FN = "hidemymods-spoof.json";

    private static boolean warnedMissingFile = false;
    private static boolean warnedParseError  = false;

    /**
     * ASM-rewritten toBytes calls this with `this.modTags` and the
     * outbound buffer. Order of entries in the spoof config is
     * preserved on the wire via LinkedHashMap.
     */
    public static void writeModList(Map<String, String> modTags, ByteBuf buffer) {
        Map<String, String> toSend = loadSpoof();
        if (toSend == null) {
            toSend = modTags;
        }
        ByteBufUtils.writeVarInt(buffer, toSend.size(), 3);
        for (Map.Entry<String, String> entry : toSend.entrySet()) {
            ByteBufUtils.writeUTF8String(buffer, entry.getKey());
            ByteBufUtils.writeUTF8String(buffer, entry.getValue());
        }
    }

    private static Map<String, String> loadSpoof() {
        File file = resolveConfigFile();
        if (!file.isFile()) {
            if (!warnedMissingFile) {
                warnedMissingFile = true;
                System.err.println("[hidemymods] spoof config not found at "
                    + file.getAbsolutePath()
                    + " -- passthrough mode, mod is a no-op");
            }
            return null;
        }

        try (Reader reader = new FileReader(file)) {
            SpoofConfig cfg = new Gson().fromJson(reader, SpoofConfig.class);
            if (cfg == null || cfg.mods == null || cfg.mods.isEmpty()) {
                return null;
            }
            Map<String, String> out = new LinkedHashMap<>(cfg.mods.size());
            for (SpoofEntry e : cfg.mods) {
                if (e != null && e.id != null && e.version != null
                    && !e.id.isEmpty() && !e.version.isEmpty()) {
                    out.put(e.id, e.version);
                }
            }
            return out.isEmpty() ? null : out;
        } catch (JsonSyntaxException jse) {
            if (!warnedParseError) {
                warnedParseError = true;
                System.err.println("[hidemymods] spoof config at "
                    + file.getAbsolutePath()
                    + " is not valid JSON -- passthrough mode: "
                    + jse.getMessage());
            }
            return null;
        } catch (Exception ex) {
            if (!warnedParseError) {
                warnedParseError = true;
                System.err.println("[hidemymods] failed to read spoof config at "
                    + file.getAbsolutePath()
                    + " -- passthrough mode: "
                    + ex);
            }
            return null;
        }
    }

    private static File resolveConfigFile() {
        String override = System.getProperty(SYS_PROP);
        if (override != null && !override.isEmpty()) {
            return new File(override);
        }
        return new File(DEFAULT_FN);
    }

    private static final class SpoofConfig {
        List<SpoofEntry> mods;
    }

    private static final class SpoofEntry {
        String id;
        String version;
    }

    private ModListSpoof() {}
}
