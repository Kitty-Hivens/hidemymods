# Design

## Hook target

Forge / NeoForge clients send their mod-list to the server as part of
the loader-specific network handshake that runs immediately after the
vanilla login phase. The exact wire format and the class responsible
for serialising it differ across the three supported versions:

| Version             | Carrier class                                                                        | Hook strategy           |
|---------------------|--------------------------------------------------------------------------------------|-------------------------|
| Forge 1.7.10        | `cpw.mods.fml.common.network.handshake.FMLHandshakeMessage$ModList`                  | ASM coremod transformer |
| Forge 1.12.2        | `net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage$ModList`        | ASM coremod transformer |
| NeoForge 1.21.1     | `net.neoforged.neoforge.network.handlers.ClientPayloadHandler` (handshake payload)   | TBD                     |

In every case the goal is the same: the client must still construct
the `Loader.modList` (or NeoForge equivalent) so that every installed
mod initialises and functions; only the over-the-wire copy of that
list is replaced by a configured one before it reaches the server.

## Implementation: configurable spoof on serialisation

The implementation hooks the serialisation path of the mod-list packet
and delegates the wire write to a runtime helper. The helper reads
`hidemymods-spoof.json` from the JVM working directory (override with
`-Dhidemymods.spoof.file=/path`) and writes the configured list onto
the wire. If the config file is missing or invalid, the helper falls
back to passthrough -- the real loaded mod-list goes out and the mod
becomes a silent no-op. Failing closed (empty list) would re-create
the v1 bug described below.

Hooking the serialiser rather than the in-memory list keeps the local
mod ecosystem intact. Other mods see the real mod-list via
`Loader.instance().getModList()`; integrations, capabilities, and
registries function normally. Only the serialised byte payload that
leaves the netty pipeline is altered.

### v1 (rejected): empty payload

The first implementation simply emptied the wire payload, on the
hypothesis that the target server runs its mod-list check as "reject
if any disallowed mods are present" and a zero-mod report would pass
trivially. Live test against SmartyCraft's Industrial server rejected
that hypothesis: the server runs the standard Forge
`FMLNetworkChecker.checkModList` "reject unless required mods are
present" path and produced a verbose `Server Mod rejections:` screen
listing every required mod-id and version. That rejection screen,
useful as it was, also confirmed that the kick is plain vanilla Forge
handshake validation rather than a custom anti-cheat, so the route
forward is to send the canonical required list rather than to evade
the check.

### v2 (current): per-instance JSON config

```json
{
  "mods": [
    {"id": "appliedenergistics2", "version": "rv6-stable-7"},
    {"id": "buildcraftlib",       "version": "7.99.24.6"}
  ]
}
```

The `id` value is the Forge mod-id (lowercase, no spaces) and must
match exactly what the server's required list expects. The display
names that appear in a rejection screen are NOT mod-ids -- they come
from each mod's mcmod.info `name` field, which is the human label.
Extract the real mod-ids from the target server's pack manifest or
from each mod jar's `mcmod.info` `modid` field.

Order in the config is preserved on the wire via `LinkedHashMap`. The
ASM transformer is independent of the spoof contents -- changing the
list is a config edit, no rebuild.

## Why ASM, not Mixin (1.12.2 / 1.7.10)

The hook target is a single method on a single class with a stable
signature in FML. The ASM transformation is a five-instruction method
body replacement that loads the instance field, the buffer parameter,
and delegates to a runtime helper. Mixin would require:

- a Mixin bootstrap coremod
- a mixin config file in resources
- a mixin processor on the Gradle classpath
- annotated mixin classes

For a single method override on a stable FML class, the Mixin
overhead is not justified. If hook complexity grows (multiple
methods, conditional replacement, cross-version refactor of the
hook set), Mixin becomes the better tool and the migration is
straightforward.

## Why not vanilla handshake bypass

The hypothesis "what if the server accepts a client that doesn't
initiate the FML handshake at all" was also considered, but the path
is much more invasive: it requires intercepting `NetworkDispatcher`
state transitions to skip the FML hello, which carries a real risk
of leaving the connection in a state the server interprets as
malformed rather than as vanilla. With v2 spoof passing the standard
Forge handshake there is no remaining motivation to pursue it.

## NeoForge 1.21.1

NeoForge replaced the FML handshake with the modern configuration-
phase payload system in 1.20.2. The class to hook is not
`FMLHandshakeMessage$ModList` (which no longer exists) but the
custom payload registered for the mod-list comparison. Identifying
the exact target needs reading NeoForge 21.1.x source; the scaffold
is in place to host the implementation once that target is fixed.
