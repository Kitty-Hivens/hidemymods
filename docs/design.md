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
list must be empty by the time it reaches the server.

## Initial implementation: empty payload on serialisation

The first implementation hooks the serialisation path of the mod-list
packet itself and replaces the on-wire list with an empty one. The
client's in-memory mod registry is not touched. From the server's
point of view, the connecting client reports zero installed mods.

This is the cheapest test of the hypothesis that the target server
runs its mod-list check as "reject if any disallowed mods are
present", in which case a zero-mod report passes trivially. If the
server's check is instead "reject unless required mods are present",
this implementation fails (empty != required set) and a replacer
implementation is needed instead. The replacer can then reuse the
same hook point and substitute a configured list rather than an
empty one.

Hooking the serialiser rather than the in-memory list keeps the local
mod ecosystem intact. Other mods see the real mod-list via
`Loader.instance().getModList()`; integrations, capabilities, and
registries function normally. Only the serialised byte payload that
leaves the netty pipeline is altered.

## Why ASM, not Mixin (1.12.2 / 1.7.10)

The hook target is a single method on a single class with a stable
signature in FML. The ASM transformation is a four-line method body
replacement (write a zero varint, return). Mixin would require:

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
initiate the FML handshake at all" is also testable, but the path
is much more invasive: it requires intercepting `NetworkDispatcher`
state transitions to skip the FML hello, which carries a real risk
of leaving the connection in a state the server interprets as
malformed rather than as vanilla. Empty mod-list is the lower-risk
PoC; vanilla-fallback is a fallback if empty mod-list is itself
rejected.

## NeoForge 1.21.1

NeoForge replaced the FML handshake with the modern configuration-
phase payload system in 1.20.2. The class to hook is not
`FMLHandshakeMessage$ModList` (which no longer exists) but the
custom payload registered for the mod-list comparison. Identifying
the exact target needs reading NeoForge 21.1.x source; the scaffold
is in place to host the implementation once that target is fixed.
