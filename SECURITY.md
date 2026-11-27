# Source and build audit / 安全审查

The original repository and every added source/resource/build file were reviewed. Scope: this repository only. This is a source review and archive inspection, not an antivirus certification or proof of absence of every possible bug.

## Mod runtime

- Standard Java, Fabric API and four client Mixins: abilities synchronization, the airborne mining check, outbound movement ground flag, and a ground-status synchronization immediately before eligible flight mining actions.
- The only writes to player abilities are `mayfly` and `flying`. No speed setter or custom physics. Only the airborne mining penalty is bypassed during actual Mod flight; tool, fatigue and underwater multipliers remain vanilla.
- No process execution, shell commands, JNI, injected processes, credential/browser/file collection, telemetry, advertising, updater or external HTTP/socket connections.
- No obfuscation, packing, FlyClip code, cheat-client dependencies, noclip, X-ray, combat, auto-building or item generation.
- No bundled dependencies, native binaries or nested JARs. GLFW is used only for the standard F7 key constant supplied by the game's normal LWJGL environment.
- Minecraft and Fabric themselves retain their normal dependencies and network behavior. The statements above describe this mod's added behavior.
- Version 1.1.0 deliberately changes the onGround flag of existing outbound movement packets only during an eligible flight/one pending landing, and sends one ground StatusOnly packet immediately before START/STOP_DESTROY_BLOCK during actual Mod flight. Otherwise packet cadence stays vanilla. The actual positions, collision flag, player onGround, velocities, speed and client fall distance are not changed. The constructor hook checks the client thread so it cannot rewrite other players' inbound packets on an integrated server. This behavior is documented as experimental, not reliable server damage immunity. Void/below-world damage is never intercepted.

## Build and release

Gradle resolves the official game and standard Fabric/Maven dependencies in the cloud. Build-time downloads are expected and are separate from mod runtime behavior. The conventional Gradle wrapper launches Java during builds; it is not executed by the installed mod.

Tool versions and action commits are pinned. The wrapper distribution checksum is committed. Build jobs have read-only repository permission; only the tag release job can write releases. No PAT is requested or stored in the repository. GitHub's job token is passed only to the release CLI.

`tools/verify_jar.py` checks the exact expected archive members, mod metadata, language keys, Mixin configuration, Java 25 class version, and selected suspicious API references. It creates the checksum and inventory. These bounded static checks supplement manual review and do not claim comprehensive malware detection.

Never disable antivirus or add an exclusion for this mod. A clean source review cannot promise Kaspersky or any other vendor will never report a false positive. Compare the published SHA-256 before use.

## Integration fixture (not shipped)

`src/smokeTest` is a separate opt-in test mod, built only by `./gradlew smokeTestJar` into `build/smoke-test/`. It requires `-Dsurvivalflight.smokeTest=true` and a disposable single-player world. It places test blocks, runs game commands, sets fixture health and checks actual server health, block destruction and void deaths. These destructive test actions and all test classes are excluded from the installable mod. Do not install or run the fixture in a valuable world. The source is public for test reproducibility.

## 简体中文

已审查本仓库全部源码、资源、Gradle 配置、Wrapper 和 Actions 工作流。Mod 写回两个飞行能力标志，并使用四个客户端 Mixin 处理能力同步、飞行挖掘、限定的着地报告和挖掘操作前的着地状态同步。着地报告修正只在自己的客户端发送线程生效，不改集成服务器中其他玩家的接收封包，不改玩家真实着地状态、位置、移动物理、生命值或虚空伤害。没有额外外联、敏感信息读取、系统命令、原生代码、加壳混淆或不相关功能；不依赖其他作弊客户端，不复制 FlyClip 代码。

构建过程会下载正常开发依赖；这些云端构建行为与 Mod 运行时行为分开。所有缓存和游戏文件不提交仓库。自动结构/字节码检查用于辅助人工审查，并非完整安全认证。无需关闭杀毒软件或添加排除项，也不保证绝无误报。

独立集成测试夹具源码位于 `src/smokeTest`，需要显式开关且仅用于可丢弃的单人世界；其中的放置测试方块、设置初始生命值等测试操作不会打包进安装 JAR。
