# SurvivalFlight v1.1.0 — Experimental / 实验性预发布

Minecraft **26.2** · Java **25** · Fabric Loader **0.20.1+** · Fabric API **0.161.0+26.2**.

Install **survivalflight-26.2-1.1.0.jar** on your client, replacing the old SurvivalFlight JAR. Do not install both versions or the sources/test-fixture JAR. The host does not need the mod. Check the attached **SHA256SUMS**.

## Changes

- Flying mining uses the same survival tool speed as on the ground: removes only vanilla's airborne ÷5 penalty. Tool, enchantment, mining fatigue and underwater rules remain vanilla.
- Experimental NoFall during actual Mod flight and one pending landing after F7 OFF. Ground reporting is cleared after landing, water/lava, riding/gliding, death, disconnect, context/mode changes or authoritative abilities sync.
- Existing outgoing movement packets get a scoped ground flag. A ground-status update precedes start/finish mining actions during actual Mod flight. Positions, velocities, collision, real client onGround and vanilla flight speed are untouched.
- **Void damage/death stays vanilla in the Overworld, Nether and End.** Ground correction stops below the current world's minimum build Y. No health, invulnerability or damage-handler changes are made.
- Packet changes are client-thread-only: integrated-server decoding and other players' packets are untouched.
- Pinned version is centralized in gradle.properties; checked push/PR/manual builds and pre-release tag publication continue. Actions use pinned Node 24 versions.
- An opt-in disposable single-player integration fixture is publicly available in src/smokeTest and excluded from the installable mod.

## Limits

This remains an **Experimental pre-release**. Client-reported grounding cannot guarantee protection on every server, against accumulated fall damage before arming, server corrections, lag or disconnects. It does not bypass third-party anticheat and does not grant server flight permission. Normal damage sources, including the void, still apply. The falling warning remains.

The new feature's behavior on the user's real remote/virtual LAN must still be checked at low altitude. Kaspersky behavior is unverified; no antivirus exclusion or disabling is required.

Actual build, client/server integration results, logs and remaining test boundaries: [VALIDATION.md](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/blob/main/VALIDATION.md).

## 简体中文

请用 **survivalflight-26.2-1.1.0.jar** 替换旧版，删除旧 JAR；不要安装源码 JAR 或独立测试夹具。房主无需安装。

- 飞行挖掘恢复为同工具的地面生存速度，保留工具、附魔、水下与挖掘疲劳规则。
- 加入实验性防摔：真正进入飞行后启用，空中按 F7 关闭后继续尝试保护至本次落地；落地、液体、骑乘/滑翔、死亡、断线、模式/世界/维度或能力同步变化会清除。
- **主世界、下界、末地掉入虚空仍会正常扣血死亡。** 读取当前维度的建筑下限，不改生命值、无敌或虚空伤害。
- 此版有范围受限的着地报告修正，不能保证所有服务端免伤，也不保证不会被踢。原版飞行速度与移动物理保持不变。

安装前核对附件 SHA-256，先按中文 README 在安全低空测试。真实远程联机的本次修正尚待验证，因此保留 Experimental / Pre-release 标记。

## Observed cloud results / 云端实测

23 regression assertions and 24 production-client/integrated-server assertions passed. Ground/flying stone mining: 11/11 test ticks. A 29-block landing after F7 OFF left server health at 20 in all three dimensions; subsequent ordinary falls damaged normally. Void damage/death remained normal in all three dimensions. Real remote multiplayer remains unverified.

23 项状态检查及 24 项集成检查通过；三维防摔落地与虚空伤害均已在临时世界分别实测。详细日志见验证报告。
