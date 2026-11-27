# SurvivalFlight 1.1.1 validation / 验证记录

Starting main revision: `03b5a13f36bba98a27a3ae7d644029211b74289d`. All work and tests ran in Codex Cloud or GitHub Actions. No local user build setup or PAT was required. Historical reports: [1.0.0](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/blob/v1.0.0/VALIDATION.md), [1.1.0](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/blob/v1.1.0/VALIDATION.md).

## Results

| Check | Observed result |
| --- | --- |
| Actual Java 25 / Gradle 9.5.1 / Loom 1.17.21 build | PASS |
| Flight flags, landing policy and hurt-Y regression assertions | PASS — 30 |
| Installable JAR metadata, exact members and Java 25 bytecode checks | PASS — 15 files, no tests, nested libraries or native payload |
| Public 1.0.0 + actual Blaze-owned SmallFireball baseline | Reproduced negative motion sync, 19-block descent to the test floor and vanilla flight ending |
| 1.1.1 actual projectile and scope-control assertions | PASS — 16 (real projectile/fire plus synthetic handler controls) |
| 1.1.1 actual projectile test altitude loss | 0.0 blocks over the 50-tick observation window; flight remained active |
| Actual server damage/ignition and horizontal knockback | Retained; final server HP 16.166666, horizontal displacement >3 blocks |
| Motion controls | Exact X/Z retained; ascent/upward impulse, Shift descent, healthy motion, other entities and F7 OFF retained |
| Full production-client/integrated-server mining/landing/void regression | PASS — 24 |
| Stone mining with same tool and between-block cooldown | 11 ground / 11 flight test ticks, server-acknowledged destruction |
| 29-block F7 OFF landings in Overworld, Nether and End | Server HP 20 in each dimension |
| Later ordinary fall after landing grace ended | Normal damage; server HP 18 in this run |
| Void with Mod flight armed in all three dimensions | Normal damage and death in each dimension |
| New version in user's real remote / virtual LAN | NOT VERIFIED |
| Kaspersky / other antivirus vendor scan | NOT VERIFIED; no certification |

Verified cloud integration-tested JAR SHA-256:

```text
60064861095f7520e2b49a0296a5091c19b4652ec840798c7f45419d4374f77f
```

The actual installable JAR was loaded from a separate game's mods directory, not project classes. The same binary was used for both fixed-version suites. The old baseline used the downloaded public 1.0.0 release JAR, verified as SHA-256 `cbce79befc1db270c862f04b06375bb7dacccca4c8668aaa71032fbe96540c27`. GitHub's tag workflow independently rebuilds and verifies the published archive/checksum; see [workflow history](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/actions/workflows/build.yml).

## Why damage caused descent

The official 26.2 `LivingEntity.knockback` keeps the old server Y velocity when the server considers the player airborne. A survival server has not granted the client's injected flight ability, so that old Y can represent falling while the client hovers. `ClientPacketListener.handleSetEntityMotion` then calls `Entity.lerpMotion` with the synchronized vector, replacing local hover velocity.

The real projectile trace captured raw Y around -2.403 in the old version, applied unchanged while hurtTime was nonzero and flying was true. Repeated fire-damage updates carried still more negative Y. In the constructed arena (hover Y=80, floor Y=61), the player dropped 19 blocks and landed; this is a scenario measurement, not a claim that every Blaze hit drops exactly 19 blocks.

With 1.1.1, the first raw vector was `(0.36403589086247923, -2.4032228529573336, 0)` and the applied vector was `(0.36403589086247923, 0, 0)`. Repeated burn syncs likewise preserved hovering. The server still registered damage and ignition. Horizontal motion remained present; total displacement differs from a falling/landing baseline because ordinary movement and floor friction are still vanilla.

## Test method and boundaries

Both baseline and candidate ran the production Fabric Knot client (`fabric.development=false`), official Minecraft 26.2, Loader 0.20.1, API 0.161.0+26.2, Temurin 25.0.5.1, Xvfb and Mesa software OpenGL. The separate opt-in fixture used a disposable command-enabled integrated world. It invoked F7/native jump input and spawned a real vanilla SmallFireball owned by a NoAI Blaze. Projectile collision, damage, ignition and motion synchronization were handled by Minecraft. The fixture's packet trace only logged raw/applied values and changed nothing.

After the real projectile measurement, synthetic handler controls explicitly seeded local motion/hurt animation state and invoked the real client motion handler. They verified exact X/Z preservation, preservation of an existing ascent and upward impulse, Shift descent, no-hurt-window passthrough, other-entity passthrough and F7 OFF. These synthetic controls supplement the projectile test; they are not additional real Blaze encounters. Test setup/commands/health initialization and all fixture classes/mixins are excluded from the installable JAR.

The main mod does not cancel damage or ignition, force flight back on, rewrite positions or lock height each tick. It only adjusts incoming negative Y for the current actively flying Mod player, during the vanilla hurt window, without Shift. It is inactive in invalid/fluid/vehicle/gliding contexts, native creative/spectator modes, after F7 OFF and below the current world's minimum Y. Position/teleport correction handlers are untouched. Unit tests cover the Y-selection function; the integration controls cover representative real handler scopes.

The independent 24-check suite then rechecked flight mining, acknowledged blocks, one-landing NoFall, a normal unprotected later fall, dimension/respawn resets, server-thread packet construction and all three void deaths. This specifically confirms that a hurt animation below the build minimum does not create void immunity.

Normal offline-profile Minecraft services and cloud audio/cursor warnings appeared; the clients/worlds ran successfully. No game binaries, exported classes, assets, worlds, caches or fixture binaries are committed.

Unverified: the user's actual remote/virtual LAN, variable latency, server corrections/plugins, different mob/projectile variants and angles, every fluid/vehicle/remapped-key combination, long building sessions and antivirus vendor behavior. This is an Experimental pre-release, not guaranteed height stability, NoFall or Anti-Kick on every server.

## Evidence

- [Old 1.0.0 projectile reproduction](docs/validation/1.1.1/baseline-1.0.0.txt)
- [Fixed 1.1.1 projectile and motion controls](docs/validation/1.1.1/hurt-1.1.1.txt)
- [Mining, landing and void regressions](docs/validation/1.1.1/regression.txt)
- [Actual cloud build and JAR inspection](docs/validation/1.1.1/cloud-build.txt)
- [Fixture compilation](docs/validation/1.1.1/fixture-build.txt)
- [Reproduce only in disposable cloud worlds](src/smokeTest/README.md)

## 简体中文

已用正式发布的 1.0.0 JAR 和烈焰人拥有的真实小火球复现：受击/燃烧同步将服务端负垂直速度带入客户端，使悬停玩家下沉。旧版在本测试平台从 Y=80 落到 Y=61；这不是所有攻击都会下降 19 格的承诺。

1.1.1 在同类小火球/燃烧场景的 50 tick 测试窗口内高度下降为 0，仍正常扣血、燃烧并保留水平击退。另用处理器输入控制检查上升、向上击退、Shift、无受伤窗口、其他实体与 F7 关闭后不被错误修正。

30 项状态/Y 策略检查、16 项受击及范围检查和 24 项挖掘/落地/虚空回归检查通过。三维 29 格落地均保持服务端 HP 20，后续普通坠落正常扣血；三个维度进入虚空仍正常扣血死亡。测试夹具不在安装 JAR 中。

你的实际远程联机尚未验证新版，因此继续标为预发布。可以从 1.0.0 直接升级，不需要先安装 1.1.0；先安全低空复测。不能保证服务端强制纠正、延迟、断线等情况下始终不掉高或免伤，也不保证杀毒软件不误报。
