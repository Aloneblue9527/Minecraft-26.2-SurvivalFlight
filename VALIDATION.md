# Validation / 验证记录

## Scope and toolchain

The initial source revision was `10e3a76470aca0d24a26e2a96d40a242054b8f35`. Its existing Actions build failed. All repairs, downloads, builds and tests were performed in Codex Cloud or GitHub Actions, not on the user's computer.

Actual build configuration: Minecraft 26.2; Temurin Java 25 (cloud smoke test: 25.0.5.1); Gradle 9.5.1; Loom 1.17.21; Loader 0.20.1; Fabric API 0.161.0+26.2. No snapshot build dependencies.

The official game JARs were downloaded using Mojang's manifest and checked against its SHA-1. The Gradle distribution and committed wrapper JAR were checked against Gradle's published SHA-256. API names were checked against actual official classes, not guessed from earlier versions.

## Build and automated checks

| Check | Result |
| --- | --- |
| Actual `./gradlew clean build --no-daemon` with Java 25 | PASS |
| GitHub Actions push and pull-request builds | PASS — [push](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/actions/runs/45987602666), [PR](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/actions/runs/45987680428) |
| Cloud vs downloaded GitHub Actions installable JAR | PASS — byte-for-byte identical |
| Dependency-free flight flag regression checks | PASS — 10 assertions |
| Installable archive metadata, expected members, language keys, Mixin and Java 25 bytecode | PASS |
| No nested dependencies, native payloads or test classes in installable JAR | PASS |
| `git diff --check` and workflow YAML parsing | PASS |
| Real server damage immunity / long-duration Anti-Kick | NOT VERIFIED; not promised |
| User's virtual LAN / remote Fabric host | NOT VERIFIED |
| Kaspersky or other antivirus vendor scan | NOT VERIFIED; no certification |

The 10 automated assertions cover default OFF, permission without automatic takeoff, restoring survival flags, repeated cleanup, preserving an existing flight grant, respecting new server grants/revocations, fresh player baselines, isolation of old/new player flags, and duplicate-enable rejection. These are **flight-flag ownership tests**, not a complete simulated Minecraft lifecycle suite.

`tools/verify_jar.py` checks an exact list of 11 archive files, Java class major version 69, metadata and selected prohibited API references. Manual review separately covered all source and build scripts. See [SECURITY.md](SECURITY.md).

Verified installable JAR SHA-256 (cloud and independent Actions build):

```text
cbce79befc1db270c862f04b06375bb7dacccca4c8668aaa71032fbe96540c27
```

## Actual production-JAR client smoke test

**PASS: the installable JAR was loaded from a temporary game's `mods/` folder**, alongside the matching Fabric API JAR. This used Fabric's production Knot client (`fabric.development=false`), the official client JAR and its normal libraries. It did not rely on loading project classes from the development output.

Cloud display: Xvfb, Mesa llvmpipe software OpenGL, 1280×720. Test profile: offline disposable `SmokeTest`; no user account credentials were used.

Observed evidence:

- Fabric Loader log: `Loading Minecraft 26.2 with Fabric Loader 0.20.1` and `survivalflight 1.0.0`.
- Actual title screen rendered as Minecraft 26.2 (Modded), then a disposable survival superflat world loaded.
- Debug Mixin export confirmed the call to `SurvivalFlightClient.serverAbilitiesReceived()` at the end of the real `ClientPacketListener.handlePlayerAbilities` method. The exported vanilla class was used only for inspection and is not distributed.
- F7 displayed `SurvivalFlight: ON`; double Space lifted the player, with survival health and hunger visible.
- Hover stayed at the same displayed Y coordinate (`-57.84185`) across screenshots with no movement input. W changed horizontal position; Shift descended to the flat floor.
- Airborne F7 disable displayed `Flight disabled — Falling danger!`.
- Survival → creative produced Mod OFF; native double-Space flight still worked. Spectator and return to survival worked, with the first F7 after returning showing ON.
- Death via the test world's `/kill`, respawn, and the first F7 afterward showed ON again, confirming the old enabled state did not survive death/player replacement.

The UI was exercised with external Java AWT Robot test input and screenshots. That test utility is **not part of the mod or repository**. No test code is bundled into the mod.

Environment warnings included unavailable audio/cursor services and unsuccessful normal Minecraft online-service calls from the disposable offline profile. The main menu and test world nevertheless rendered and ran. Initial attempts before the official asset download completed were incomplete; they were superseded by the final smoke run.

## Remaining test boundaries

This is a limited single-player integrated-server smoke test, **not real remote multiplayer success**. Not verified: virtual-LAN IP/port joining, dedicated-server corrections/kicks, long building sessions, protected high falls, abrupt remote disconnect/reconnect at height, complete dimension-transition coverage, every remapped key/language UI, other flight mods, or vendor antivirus results.

Compilation, loader recognition, graphical client/world loading and the observed low-altitude controls are distinct from these unperformed tests. v1.0.0 must remain an **Experimental pre-release**.

For release builds, GitHub Actions independently recompiles, runs the same 10 checks, inspects the JAR and attaches its actual SHA-256. The tag workflow publishes only on successful checks. The exact commit, workflow logs, release assets and fingerprint are available from the [workflow history](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/actions/workflows/build.yml) and [v1.0.0 release](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/releases/tag/v1.0.0).

## 简体中文

已在云端实际编译成功，10 项飞行标志回归断言与最终 JAR 结构检查通过。已使用成品 JAR（非开发输出目录）在 Fabric 正式加载路径启动至主菜单及临时单人生存超平坦世界；验证 F7 提示、双空格起飞、悬停、水平移动、Shift 下降、空中关闭警告、创造/旁观者/生存切换、死亡重生后重新开启。Mixin 注入也已检查到实际目标方法。

这些结果不等于真实多人联机通过。你的虚拟局域网、远程服务器、长时间建筑、高空断线与可靠免伤仍未验证，不宣称完全稳定。版本必须保留 Experimental / Pre-release 标记，并按 README 先做低空验收。没有要求本地开发环境、PAT、关闭杀毒软件或添加排除项。
