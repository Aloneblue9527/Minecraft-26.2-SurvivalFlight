# SurvivalFlight 1.1.0 validation / 验证记录

The starting source was main revision `35454093444673a9cef4a5c4e30884af257b0d9b`. All development, builds and integration tests ran in Codex Cloud or GitHub Actions. No local user development environment or PAT was required. Historical 1.0.0 results remain available in the [1.0.0 tagged report](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/blob/v1.0.0/VALIDATION.md).

## Results

| Check | Observed result |
| --- | --- |
| Java 25 / Gradle 9.5.1 / Loom 1.17.21 actual build | PASS |
| Flight capability and landing-policy regression assertions | PASS — 23 |
| Production JAR metadata, Java 25 bytecode, exact file list, selected API checks | PASS — 15 files, no nested libraries, native files or test classes |
| Production client + disposable integrated server fixture | PASS — 24 integration assertions |
| Real ground vs flight stone mining, identical tool and between-block cooldown | 11 vs 11 test ticks; server acknowledged both broken blocks |
| F7 OFF, 29-block landing in Overworld | Server health 20 |
| F7 OFF, 29-block landing in Nether | Server health 20 |
| F7 OFF, 29-block landing in End | Server health 20 |
| Later ordinary fall after grace ended | Normal damage; server health 18 in final run |
| Void in Overworld, Nether and End with Mod flight armed | Normal damage and death in all three |
| Packet construction on integrated server thread | Original false flag preserved; no other-player/global server rewrite |
| New behavior on user's real remote / virtual LAN | NOT VERIFIED |
| Kaspersky / other vendor scan | NOT VERIFIED; no certification |

Installable JAR SHA-256:

```text
092e46d52d7b66d0256dc9d2b75ed625300e81c71558efe94f6a3eabc2afc251
```

The exact installable JAR was copied into the test client's mods folder and compared byte for byte with the cloud build. GitHub's tag workflow independently rebuilds, verifies the archive and checksum before publishing. Workflow records: [Build SurvivalFlight](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/actions/workflows/build.yml).

## Test method and limits

The production Fabric Knot client used Minecraft 26.2, Loader 0.20.1, Fabric API 0.161.0+26.2, Temurin 25.0.5.1, Xvfb and software Mesa OpenGL. It loaded the actual installable mod JAR with `fabric.development=false`. A separately compiled, opt-in client test fixture (`src/smokeTest`) drove the disposable single-player world. Neither fixture classes nor commands/health initialization are present in the installable JAR.

The fixture invoked F7 via vanilla KeyMapping and native double-jump input, kept the actual local physical onGround false while hovering, checked the normal netherite-pickaxe stone speed (9.0), and measured actual server-acknowledged block breaks. Both compared blocks had the same vanilla between-block cooldown. Server raw mining speed sampled between actions may still be 1.8 while physically airborne; the fresh ground-status update at start/finish mining is what makes those specific server calculations consistent. The test checks accepted destruction, not just the client's visual cracks or a sampled speed value.

It then tested real 29-block falls after F7 OFF and checked actual server health in each dimension. After grace ended, a later ordinary fall damaged the player, confirming no lingering always-on immunity. Void tests rearmed Mod flight first, teleported below `getMinY()-64`, observed health loss and death in all three dimensions, then respawned. Test platforms were explicitly chunk-loaded; the End platform was away from the exit portal/central structures. Dimension/respawn resets required F7 again. No damage handlers or invulnerability flags were modified.

The server's actual `Player.getDestroySpeed` airborne /5 check, movement ground/fall handling and `Entity.checkBelowWorld` (`Y < getMinY()-64`) were inspected in the official 26.2 bytecode. Mixins are limited to the current client player and main-thread outgoing packets. An explicit server-thread construction test confirms the packet hook does not alter shared integrated-server decoding.

The 23 lightweight checks cover previous capability restoration plus first-flight arming, pending landing after F7 OFF, clearing at landing/reset, ineligible contexts and per-world minimum-Y boundaries. They do not simulate every network/lifecycle combination.

Normal Minecraft offline-profile service calls, missing cloud audio/cursor services and the optional Mixin debug decompiler produced environment warnings. The game world and assertions ran successfully. No Minecraft binaries, assets, exported classes, test worlds, caches or test-fixture binaries are committed.

Unverified: latency/reordering/corrections on real remote servers, third-party anticheat, drops already accumulated before arming, abrupt disconnects at height, extended building sessions, every water/vehicle/gliding combination, language/remapping UI and antivirus vendors. These observed successes do **not** guarantee universal NoFall or Anti-Kick. The release remains Experimental / Pre-release.

## Saved evidence

- [Actual cloud build and JAR inspection](docs/validation/1.1.0/cloud-build.txt)
- [Fixture compilation](docs/validation/1.1.0/fixture-build.txt)
- [Full client/server integration log — ALL PASS: 24 checks](docs/validation/1.1.0/client-smoke.txt)
- [Reproduce only in a disposable world](src/smokeTest/README.md)

## 简体中文

1.1.0 已实际编译，23 项能力/落地保护回归检查和 24 项实际客户端/服务端集成检查通过；成品 JAR 的 15 个文件和 Java 25 字节码已检查，无测试类、嵌套依赖或原生文件。

同工具、相同连续挖掘冷却条件下，地面与飞行挖石头都用了 11 个测试 tick，且检查了服务端方块确实被破坏。主世界、下界、末地开启飞行后关闭 F7，从 29 格高度落地均保持服务端生命值 20；保护结束后的普通坠落正常扣血。三个维度重新开启飞行后进入虚空，均正常扣血死亡。

这些是可丢弃的云端单人世界实测，不能代替你的真实远程联机验证。测试夹具中的生命值初始化/世界命令仅用于构造测试条件，不在安装 JAR 内。新版本仍标为预发布，服务端纠正、延迟、断线和启用前已累积的摔落距离仍有风险；不要关闭杀毒软件或添加排除项。
