# SurvivalFlight

[简体中文](README.zh-CN.md) · [Releases](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/releases) · [Cloud builds](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/actions/workflows/build.yml)

**Experimental client-only building flight for Minecraft 26.2 / Fabric / Java 25.**
Use only in private worlds with the host's permission. v1.1.2 is a **pre-release**: the new mining/landing/hurt-motion behavior in real remote multiplayer has not been verified. This does not grant server-side flight permission. Fall protection is experimental and cannot guarantee immunity.

## Install and use

1. Select **Minecraft Java Edition 26.2** in your launcher and install **Fabric Loader 0.20.1 or newer**.
2. Install **Fabric API 0.161.0+26.2** for Minecraft 26.2.
3. Download `survivalflight-26.2-1.1.2.jar` from the release into **your client's** `mods` directory. Do not install the `-sources.jar`.
4. Check the download against the release's `SHA256SUMS`. PowerShell: `Get-FileHash .\survivalflight-26.2-1.1.2.jar -Algorithm SHA256`.
5. Join the permitted private world. The host does **not** need this mod.

Flight starts **OFF**. Press **F7** to enable flight permission, then **double-tap Space** to enter vanilla creative flight. WASD moves horizontally, Space ascends, Shift descends. Double-tap Space again to stop flying, or F7 to turn the feature off. Remap F7 under vanilla **Options → Controls → Key Binds → Miscellaneous**.

The mod never changes flight speed, sprint handling, collision, health, hunger, or inventory. Survival building permissions, tool drops and other interactions remain vanilla. Vanilla creative movement remains responsible for hovering and movement. Normal survival permissions still apply to building.

Only the vanilla Action Bar is used, with English and Simplified Chinese translations:

- `SurvivalFlight: ON`
- `SurvivalFlight: OFF`
- `Flight disabled — Falling danger!`

The falling warning is conservative: it appears when disabling while airborne, even near the ground. It also appears when the mod observes unexpected flight loss while airborne. It is a warning, **not protection**; disconnecting prevents further messages.

## Sprint + Shift movement (1.1.2)

A protected airborne position update can carry a ground flag for experimental landing protection. Combined with Shift, the unmodified survival server can interpret that as grounded sneaking and apply vanilla edge braking, causing repeated position corrections. The published 1.1.1 reproduced this.

Before each **protected, physically airborne Shift position update**, 1.1.2 sends one truthful airborne StatusOnly update. The original position update and its landing-protection flag follow normally. Rotation-only/status-only packets cannot recurse through this path. Grounded sneaking, normal flight, input, position coordinates and native sprint/flight speeds are unchanged. Incoming server corrections are still processed normally. The extra status is limited to the affected input/protection scope, including one pending landing after F7 OFF.

## Hurt-related hovering (1.1.1)

A survival server does not share the client's injected flight permission. After a hit, its motion update can contain a falling vertical speed even though the client was hovering. Version 1.1.1 stabilizes **only negative vertical motion received during vanilla's hurt animation**, while this mod is actively flying in survival and Shift is not held. The existing local ascent is preserved.

Damage, fire, horizontal knockback, upward knockback and intentional Shift descent remain vanilla. Ordinary motion outside the hurt window, other entities and server position/teleport corrections are untouched. The hook is inactive in fluids, vehicles, gliding, native creative/spectator modes, after F7 OFF and below the world's minimum Y. This is not a permanent altitude lock or general knockback immunity.

You can upgrade directly from 1.0.0 to 1.1.2; it includes the 1.1.0 mining and experimental landing features below. Replace the old JAR rather than installing two versions.

## Server limitations: Anti-Kick and NoFall

### Flight mining and experimental landing protection (1.1.0)

While actually flying with this mod in survival, the airborne mining penalty (vanilla speed ÷5) is skipped. Mining uses the same tool speed as on the ground; tool choice, enchantments, fatigue and underwater rules remain vanilla.

During an eligible Mod flight, ordinary movement packets report `onGround=true` so the unmodified server can evaluate mining/landing without the usual airborne penalty. A single ground-status update also precedes each start/finish mining action during actual Mod flight so the server evaluates that action consistently. Normal position update cadence stays vanilla; protected airborne Shift position updates have the additional airborne-status preface described above. The ground-report/mining assist leaves the real client's physical `onGround`, positions, flight speed, health and damage handlers untouched. The hurt-motion adjustment above is separately scoped. No downward pulses or client fall-distance clearing are used.

Protection arms only after actual flight, and stays armed for the next landing if F7 is turned off in the air. It ends at landing, water/lava, riding/gliding, death, disconnect, world/dimension or game-mode changes, or an incoming abilities sync. Simply enabling F7 before an ordinary jump does not arm it. The falling warning remains because the server can reject/correct the report.

**Void damage stays vanilla in the Overworld, Nether and End.** Below the current world's `getMinY()` the ground-report correction stops. No dimension-specific invulnerability or damage cancellation exists. Vanilla's below-world damage check is independent of landing/fall distance.

This is a deliberate, narrowly scoped experimental ground-report change requested for flight mining/NoFall. It is not a generic anticheat bypass. Lag, prior accumulated fall damage before flight is armed, server corrections and different server behavior can still cause injury or death. Test only in a backed-up disposable world first.

The actual 26.2 server bytecode was inspected:

- `MinecraftServer.allowFlight()` returns true for the base server used by a normal single-player **Open to LAN** integrated server. The dedicated server overrides this using `allow-flight`.
- `ServerGamePacketListenerImpl` checks floating using server flight permission, movement, nearby blocks and other conditions. A dedicated server with `allow-flight=false` can kick you. On a dedicated server, ask the host to enable `allow-flight=true` using their normal administration process; a host's verbal permission alone cannot change the setting.
- The server accepts a client's flying flag only if **its own** player abilities allow flight. Locally setting `mayfly` does not grant this permission.
- Fall damage is evaluated on the server from movement and server state. `allow-flight=true` disables the floating kick check; it does **not** grant survival fall immunity. Descent, landing, F7, death, dimension changes, ability synchronization, connection loss and reconnecting can still lead to damage or death. Do not rely on the mod while offline or reconnecting at height.
- Movement correction, latency, other mods, and server configuration may still interrupt flight. No absolute anti-kick or antivirus promise is made.

The host does not need this mod. The ground-report correction is experimental and is not a generic anticheat bypass. If this client-only behavior is insufficient for your world, safe authoritative flight/fall protection requires host/server cooperation outside this mod's scope.

## State management

Only survival mode is eligible. Death, player/world/dimension replacement, disconnect, reconnect and game-mode changes reset the feature. It must be explicitly enabled again. Stale key presses are discarded at context changes.

Disabling restores the two original flight flags. A client Mixin observes **after** vanilla applies a server abilities packet: it drops the saved override and turns the feature OFF, preserving the server's fresh flags. Creative and spectator abilities remain under vanilla control. Speed and other abilities are never saved or rewritten. Other client mods that also change flight flags have not been verified.

## Low-altitude acceptance test

Use a backed-up disposable private world with the host's permission. Keep valuable items elsewhere. Start only **one or two blocks** above a safe flat floor, with water available.

1. Confirm OFF after joining. Press F7; confirm ON and editable key binding. Confirm double Space, WASD, Space, Shift, hovering and building.
2. Land and disable. Confirm normal survival movement, hunger, health and block interaction.
3. One block above the floor, press F7 and confirm the falling warning. Do not test an unprotected high drop.
4. Test double Space stopping flight, death/respawn, dimension travel, disconnect/rejoin, and survival → creative → spectator → survival. Confirm OFF after resets and normal native flight.
5. If the host permits, check a server abilities update resets OFF. Test longer hovering only near a safe landing surface; record kick/correction behavior.
6. Do not move to high construction until the actual LAN setup is independently validated. Use water, scaffolding, Feather Falling and backups; none makes this mod a NoFall guarantee.

## Cloud builds and reproducibility

No local development setup is needed: **Actions → Build SurvivalFlight → Run workflow**, then download the successful `SurvivalFlight-<commit SHA>` artifact. Pushes, pull requests and manual runs compile, execute regression checks, inspect the JAR and upload its SHA-256.

The committed Gradle wrapper pins **Gradle 9.5.1** and verifies its distribution checksum. Dependencies are fixed: **Loom 1.17.21**, **Loader 0.20.1**, **Fabric API 0.161.0+26.2**, **Minecraft 26.2**, **Java 25**. No mappings dependency is needed for the unobfuscated 26.2 game. Archives use sorted entries and fixed timestamps.

For cloud maintainers: `./gradlew clean build --no-daemon`, then `python3 tools/verify_jar.py`. Build output is in `build/libs/`. Only the small standard Gradle wrapper JAR is committed; game files, caches and build output are ignored.

A version tag matching `mod_version` in `gradle.properties` runs the same checks, then publishes an **Experimental pre-release** with the installable JAR, sources JAR, checksum and archive inventory. The release job has write permission; ordinary build jobs have read permission. Actions are pinned to commit SHAs.

See [VALIDATION.md](VALIDATION.md) for actual build/load results, [SECURITY.md](SECURITY.md) for the source audit, and [RELEASE_NOTES.md](RELEASE_NOTES.md) for release limitations.

## Official compatibility evidence

- [Mojang version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json) and [26.2 metadata](https://piston-meta.mojang.com/v1/packages/e1727c77c55b6a2eba6017591e866a02f3f5cb8d/26.2.json): released game; `javaVersion.majorVersion=25`.
- [Fabric's 26.2 migration article](https://fabricmc.net/2026/06/15/262.html): Loom 1.17, Gradle 9.5.1, unobfuscated names and GUI API changes.
- [Fabric Loader metadata for 26.2](https://meta.fabricmc.net/v2/versions/loader/26.2), [Loom Maven metadata](https://maven.fabricmc.net/net/fabricmc/fabric-loom/maven-metadata.xml), [Fabric API Maven metadata](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml).
- [Gradle 9.5.1 compatibility](https://docs.gradle.org/9.5.1/userguide/compatibility.html): Java 25 is supported.
- Actual official 26.2 class signatures and bytecode, verified by compilation: `KeyMapping`, `LocalPlayer.sendOverlayMessage`, `Abilities.mayfly/flying`, `ClientPacketListener.handlePlayerAbilities`. Actual Fabric artifacts supply `KeyMappingHelper`, `ClientTickEvents` and `ClientPlayConnectionEvents`.

MIT licensed; see [LICENSE](LICENSE). Not an official Minecraft product.
