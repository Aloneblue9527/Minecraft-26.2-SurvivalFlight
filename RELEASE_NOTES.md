# SurvivalFlight v1.1.1 — Experimental / 实验性预发布

Minecraft **26.2** · Java **25** · Fabric Loader **0.20.1+** · Fabric API **0.161.0+26.2**.

Install **survivalflight-26.2-1.1.1.jar**, replacing your older SurvivalFlight JAR. You can upgrade directly from 1.0.0; there is no need to install 1.1.0 first. Do not install multiple versions, the sources JAR or the disposable test fixture. The host does not need this mod.

## Hurt-related hovering fix

Reproduced against the public 1.0.0 JAR using a real Blaze-owned SmallFireball: the survival server's motion sync carried its falling Y velocity into an otherwise hovering client, with repeated syncs during fire damage. This could drop the player far enough to land and end vanilla flight.

During active Mod flight and vanilla's hurt animation, a negative incoming Y is stabilized if Shift is not held. X/Z, existing ascent and upward motion are retained. Damage, fire and death remain vanilla. Ordinary motion outside the hurt window, other entities, manual Shift descent, F7 OFF, native creative/spectator modes and below-world motion are not filtered. Server teleports/position corrections remain authoritative; this is not a permanent height lock or general knockback immunity.

Includes the 1.1.0 ground-rate flight mining and experimental one-landing NoFall behavior. Void damage/death remains vanilla in the Overworld, Nether and End. Existing server/latency/disconnect limitations still apply. No speed change, noclip, health/invulnerability modification, native payload, extra external network connection or cheat-client dependency was added.

## Verification and limits

Actual Java 25 build and exact production JAR checks passed. See [VALIDATION.md](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/blob/main/VALIDATION.md) for old/new projectile traces, real damage, scope controls, mining/landing/void regressions and remaining boundaries. The opt-in fixture and its motion/damage setup are not shipped in the mod.

The new version has not been tested in the user's real remote/virtual LAN. It remains an Experimental pre-release. Start at low altitude; client-side protection cannot guarantee every server correction, lag, prior fall accumulation or disconnect case. Kaspersky behavior is unverified; no antivirus exclusion or disabling is required. Check the attached SHA256SUMS.

## 简体中文

请用 **survivalflight-26.2-1.1.1.jar** 替换旧 JAR，可从 1.0.0 直接升级，依赖不变，无需先安装 1.1.0。不要同时安装两个版本，也不要安装源码 JAR 或测试夹具。

已复现并修正烈焰人/燃烧受击同步导致的飞行下沉：只在本 Mod 实际飞行、受伤窗口内且没有主动下降时处理负垂直速度。正常扣血、燃烧、水平/向上击退、Shift 下降和服务端传送纠正保留；不做永久锁高或通用击退免疫。

此版包含 1.1.0 的挖掘与实验性防摔功能；主世界、下界、末地的虚空伤害和死亡仍保持原版。云端实测与真实联机验证分别记录，新版本仍为 Experimental / Pre-release，请先安全低空复测。

## Observed cloud results / 云端实测

30 unit/state assertions, 16 hurt/projectile/scope checks and 24 mining/landing/void regression checks passed. The old 1.0.0 JAR reproduced the Blaze-induced descent; the 1.1.1 projectile/fire observation recorded 0.0-block altitude loss while damage, fire and horizontal knockback remained. The three-dimensional landing and void tests still passed.

旧版受击下沉已复现，修正版实测不掉高且仍正常扣血、燃烧和水平击退；三维落地与虚空回归均通过。实际远程联机仍待验证。
