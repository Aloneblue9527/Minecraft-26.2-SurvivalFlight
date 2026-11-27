# SurvivalFlight v1.0.0 — Experimental / 实验性预发布

Minecraft **26.2**, Fabric Loader **0.20.1+**, Fabric API **0.161.0+26.2**, Java **25**.

Install **survivalflight-26.2-1.0.0.jar** on the joining player's client. The host does not need this mod. Do not install the sources JAR. Compare the download with the attached **SHA256SUMS**.

## Features and fixes

- F7 enables/disables survival flight permission; remappable in vanilla key bindings.
- Vanilla double Space / WASD / Space / Shift creative-flight movement; no added speed or collision changes.
- English and Simplified Chinese Action Bar status and airborne-disable warning.
- Reset on death, changed player/world/dimension, reconnect and game-mode changes; preserve native creative/spectator flight.
- Respect incoming authoritative abilities by resetting OFF without restoring stale flags. Use vanilla ability notification when F7 restores a changed flying flag.
- Remove unreliable periodic downward impulses, forged ground packets and client fall-distance clearing.
- Fixed stable cloud toolchain, checked Gradle wrapper, regression checks, JAR inspection and checksum artifacts. Public MIT source; no obfuscation, extra external networking, telemetry, native payloads or cheat-client dependencies.

## Known limitations

**No reliable NoFall or universal Anti-Kick.** The server owns flight authorization and damage. Ordinary Open to LAN uses the integrated server's allowed-flight behavior; dedicated servers may require the host to enable `allow-flight=true`. That setting alone does not grant survival flight immunity. Host permission cannot change server configuration by itself.

Disabling or losing flight, descending/landing, server corrections, ability updates, dimension transitions, disconnects and reconnecting at height may cause damage or death. The warning is not protection. Do not use at building height until your own LAN setup passes the low-altitude test in the README.

**Real remote multiplayer, the user's virtual LAN, and Kaspersky behavior are unverified.** This is a pre-release, not a fully stable release. No antivirus software needs to be disabled and no exclusion should be added.

Build, automated checks and cloud client-load evidence are separately reported in [VALIDATION.md](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/blob/v1.0.0/VALIDATION.md). The release workflow only publishes after its Gradle checks and archive inspection pass. The checksum is calculated from the attached JAR, not an estimated value.

## 简体中文

仅安装到加入游戏的客户端，房主无需安装。默认关闭，F7 开启权限后双击空格飞行，使用原版操作与速度；可在原版按键设置修改快捷键。没有额外加速、穿墙或无关功能。

已修复构建配置、26.2 API 与能力恢复，并移除无法保证安全的下沉/伪造着地逻辑。只有 Action Bar 状态与空中关闭警告，没有复杂 GUI。

**不能保证不被踢或免坠落伤害。** 普通“开放到局域网”与独立服务器的悬空策略不同；独立服务器可能需要房主设置 `allow-flight=true`，但这并不提供生存坠落免伤。关闭、失去飞行、下降着地、能力同步、切换维度、断线或高空重连均可能受伤或死亡。警告不等于保护。

真实远程多人、你的虚拟局域网和 Kaspersky 均未验证，因此明确标为实验性预发布。请先备份世界，在平地上方 1–2 格完成 README 的低空验收流程，确认死亡重生、维度、重连和游戏模式切换的行为。安装前按附件 `SHA256SUMS` 核对 JAR 指纹。不要关闭杀毒软件或添加排除项。
