# Source and build audit / 安全审查

The original repository and every added source/resource/build file were reviewed. Scope: this repository only. This is a source review and archive inspection, not an antivirus certification or proof of absence of every possible bug.

## Mod runtime

- Standard Java, Fabric API and one Sponge Mixin into the client abilities handler.
- The only writes to player abilities are `mayfly` and `flying`. No speed setter or custom physics.
- No process execution, shell commands, JNI, injected processes, credential/browser/file collection, telemetry, advertising, updater or external HTTP/socket connections.
- No obfuscation, packing, FlyClip code, cheat-client dependencies, noclip, X-ray, combat, auto-building or item generation.
- No bundled dependencies, native binaries or nested JARs. GLFW is used only for the standard F7 key constant supplied by the game's normal LWJGL environment.
- Minecraft and Fabric themselves retain their normal dependencies and network behavior. The statements above describe this mod's added behavior.
- The original forged movement packets and client fall-distance reset were removed. Server authority and fall risk are documented.

## Build and release

Gradle resolves the official game and standard Fabric/Maven dependencies in the cloud. Build-time downloads are expected and are separate from mod runtime behavior. The conventional Gradle wrapper launches Java during builds; it is not executed by the installed mod.

Tool versions and action commits are pinned. The wrapper distribution checksum is committed. Build jobs have read-only repository permission; only the tag release job can write releases. No PAT is requested or stored in the repository. GitHub's job token is passed only to the release CLI.

`tools/verify_jar.py` checks the exact expected archive members, mod metadata, language keys, Mixin configuration, Java 25 class version, and selected suspicious API references. It creates the checksum and inventory. These bounded static checks supplement manual review and do not claim comprehensive malware detection.

Never disable antivirus or add an exclusion for this mod. A clean source review cannot promise Kaspersky or any other vendor will never report a false positive. Compare the published SHA-256 before use.

## 简体中文

已审查本仓库全部源码、资源、Gradle 配置、Wrapper 和 Actions 工作流。Mod 仅写两个飞行标志，使用一个客户端能力同步 Mixin。没有额外外联、敏感信息读取、系统命令、原生代码、加壳混淆或不相关功能；不依赖其他作弊客户端，不复制 FlyClip 代码。

构建过程会下载正常开发依赖；这些云端构建行为与 Mod 运行时行为分开。所有缓存和游戏文件不提交仓库。自动结构/字节码检查用于辅助人工审查，并非完整安全认证。无需关闭杀毒软件或添加排除项，也不保证绝无误报。
