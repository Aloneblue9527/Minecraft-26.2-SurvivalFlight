# SurvivalFlight 1.1.2 validation / 验证记录

Starting main revision: `ad1ef666b5c7a28035ee6ffc1c7b2eda18525144`. The user reported repeated corrections on 1.1.1 when sprint-flying with Shift + forward. Development/builds/tests ran in Codex Cloud or GitHub Actions. No local user development setup or PAT was required. Historical reports remain in the [1.1.1 tag](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/blob/v1.1.1/VALIDATION.md) and earlier tags.

## Observed results

| Check | Result |
| --- | --- |
| Actual Java 25 / Gradle 9.5.1 / Loom 1.17.21 build | PASS |
| Flight/landing/hurt-Y/airborne-preface state assertions | PASS — 36 |
| Installable JAR metadata, exact archive members and Java 25 bytecode | PASS — 15 files; no fixtures, native payload or nested libraries |
| Public 1.1.1, sprint + forward control (40 ticks) | 0 server corrections, horizontal displacement 32.79887164336382 |
| Public 1.1.1, sprint + Shift + forward (100 ticks) | 4 real server corrections plus `moved wrongly` warnings; reproduced |
| 1.1.2, identical sprint + forward control | 0 corrections, identical displacement 32.79887164336382 |
| 1.1.2, sprint + Shift + forward | 0 corrections, continued forward/descent, native sprint and flight active |
| 1.1.2, non-sprint Shift + forward | 0 corrections; forward/descent preserved |
| Shift release, repeated press/release, sprint + Space ascent | 0 corrections; expected native movement preserved |
| Shift + forward pending landing after F7 OFF | 0 corrections, server HP 20, original survival flight flags restored |
| Movement/correction integration assertions | PASS — 25 |
| Real Blaze-owned projectile/fire + synthetic motion scope controls | PASS — 16; altitude loss 0.0, damage/fire/horizontal knockback retained |
| Production-client/integrated-server mining/landing/void regression | PASS — 24 |
| Same-tool stone mining, equivalent between-block cooldown | 11 ground / 11 flight test ticks, actual server block destruction |
| 29-block F7 OFF landings in Overworld, Nether, End | Server HP 20 in each |
| Later normal fall after grace ended | Normal damage |
| Void with Mod flight armed in all three dimensions | Normal damage and death |
| New behavior on user's actual remote/virtual LAN | NOT VERIFIED |
| Kaspersky / vendor scan | NOT VERIFIED; no certification |

Cloud-tested installable JAR SHA-256:

```text
1980d5da2389660498a39363095b4641f58c121e41cb6415a2632bde9daca5f9
```

The exact same installable JAR was loaded from a separate temporary game's mods folder for all three fixed-version integration suites. The old baseline was the public 1.1.1 release JAR, checked as SHA-256 `60064861095f7520e2b49a0296a5091c19b4652ec840798c7f45419d4374f77f`. GitHub's tag workflow independently rebuilds and checks the archive/checksum before publishing; see [workflow history](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/actions/workflows/build.yml).

## Cause and scoped change

The official 26.2 `Player.maybeBackOffFromEdge`/`isAboveGround` methods apply edge braking when a non-flying server player is sneaking and considered grounded. `ServerGamePacketListenerImpl.handlePlayerInput` propagates Shift to the server player. A survival server has not granted the client's injected flight ability. The experimental ground report could therefore activate grounded sneak-edge rules during a real airborne forward/descent move, producing vanilla `moved wrongly` corrections.

Before each protected physically airborne Shift position packet, 1.1.2 sends a truthful StatusOnly(false) to clear that stale grounded interpretation for the movement step. The original position and its existing landing report follow normally. Positions, velocities, sprint multiplier, physical client onGround and normal position cadence are not changed. A main-thread construction guard keeps the preparatory false flag intact and is restored in finally. Status/rotation-only packets cannot recursively prepare another move. The predicate excludes non-protected, actually grounded and non-Shift cases. Existing world/mode/fluid/vehicle/below-world guards remain.

Incoming position corrections are still processed by vanilla. The fixture counts those real packets after processing, without cancelling or modifying any correction. The source does not fake sprint/Shift input, cap movement speed, disable server checks or change collision. Improved progress during the fixed combination comes from removal of the reproduced rollback, not an additional movement multiplier.

## Method and test boundaries

The production Fabric Knot client used `fabric.development=false`, official Minecraft 26.2, Loader 0.20.1, API 0.161.0+26.2, Temurin 25.0.5.1, Xvfb and Mesa software OpenGL. Separate opt-in fixtures ran only in disposable command-enabled integrated worlds. No fixtures, fixture commands, test worlds, game binaries/assets, exported classes or caches are bundled/committed as mod content.

The movement fixture uses native KeyMapping forward/sprint/Shift/jump states and confirms sprint was actually active before Shift. It excludes planned setup teleports from correction counts, then counts all server position corrections through the measured input windows. Shift-release hover displacement is measured after native inertia settles; corrections are counted through the full release transition. Repeated Shift transitions and the F7 OFF landing have no test teleport inside their measurement windows.

The 16-check hurt suite reuses an actual vanilla SmallFireball owned by a NoAI Blaze, followed by explicitly synthetic motion-handler controls. The 24-check suite rechecks accepted block destruction, NoFall's one landing, subsequent ordinary fall damage, dimension/respawn resets, unmodified server-thread packet construction and normal void deaths. Fixture health initialization is test setup, not a production immunity mechanism.

The first attempt to start the baseline client failed because the resumed cloud display was unavailable. A new disposable display was started and the successful baseline/fixed runs supersede that environment-only failure. Normal offline-profile services and missing audio/cursor services can emit warnings; the clients/worlds and final assertions ran successfully.

Unverified: the user's real virtual LAN, latency/reordering/server corrections or plugins, long construction sessions, every terrain-edge/collision/input-remapping combination, and antivirus vendors. These tests do not guarantee universal absence of correction, NoFall or Anti-Kick. The release remains Experimental / Pre-release; normal server authority and collision remain active.

## Evidence

- [Published 1.1.1 reproduction](docs/validation/1.1.2/baseline-1.1.1.txt)
- [1.1.2 movement/correction suite — 25 checks](docs/validation/1.1.2/movement.txt)
- [1.1.2 hurt/projectile/scope suite](docs/validation/1.1.2/hurt.txt)
- [Mining/landing/void regressions](docs/validation/1.1.2/regression.txt)
- [Actual cloud build and JAR inspection](docs/validation/1.1.2/cloud-build.txt)
- [Fixture compilation](docs/validation/1.1.2/fixture-build.txt)
- [Reproduce only in disposable cloud worlds](src/smokeTest/README.md)

## 简体中文

正式 1.1.1 的“冲刺 + Shift + 前进”已复现 4 次真实服务端纠正。1.1.2 同组合纠正为 0；普通 Shift 前进、反复切换 Shift、松开后继续冲刺、冲刺上升及空中关闭 F7 后的 Shift 前进落地也为 0。普通冲刺对照的位移与旧版一致，未增加速度，也未忽略服务端纠正。

36 项状态检查、25 项移动组合检查、16 项受击检查、24 项挖掘/落地/虚空检查通过。受击仍正常扣血/燃烧/水平击退且测试窗口不掉高；三个维度的 29 格落地均保持 HP 20，后续普通坠落和三维虚空死亡仍正常。

测试均为云端可丢弃的集成世界，不代表你的真实局域网已经通过。新版继续标为预发布，先低空复测；不保证任意延迟、服务器纠正或地形碰撞条件下都不回弹或免伤，也不要求关闭杀毒软件或添加排除项。
