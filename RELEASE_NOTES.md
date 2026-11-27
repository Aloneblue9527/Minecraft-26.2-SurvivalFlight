# SurvivalFlight v1.1.2 — Experimental / 实验性预发布

Minecraft **26.2** · Java **25** · Fabric Loader **0.20.1+** · Fabric API **0.161.0+26.2**.

Replace the old mod with **survivalflight-26.2-1.1.2.jar**. You can upgrade directly from any earlier SurvivalFlight release; dependencies are unchanged. Do not install duplicate versions, the sources JAR or the disposable fixture. The host does not need this mod. Verify the attached **SHA256SUMS**.

## Sprint + Shift + forward correction fix

The public 1.1.1 JAR reproduced repeated vanilla position corrections while sprint-flying forward and descending with Shift. The experimental ground report could make the survival server treat this as grounded sneaking, activating vanilla edge braking and `moved wrongly` corrections. The earlier tests had not covered this combination.

Before each protected, physically airborne Shift position update, the client now sends one truthful airborne status update. The original position update, coordinates and landing-protection flag follow normally. A main-thread construction guard prevents the preparatory false flag being rewritten; try/finally restores the guard. Rotation/status-only packets cannot recurse. The actual input, flight/sprint speeds, positions, normal position cadence and physical collision remain vanilla. Incoming server corrections are not ignored or hidden.

The correction is limited to the affected protected/airborne/Shift/position scope, including one pending landing after F7 OFF. Ordinary flight, actual grounded sneaking, native creative/spectator flight, fluids/vehicles/gliding and below-world movement are not given a generic bypass.

Includes 1.1.0 mining/experimental landing protection and 1.1.1 hurt-hover stabilization. Normal health damage, fire, horizontal/upward knockback, intentional descent and void death remain. Client-reported protection is still experimental: server settings, corrections, latency, prior fall accumulation and disconnects can cause damage or movement interruption. The new build still requires the user's real LAN low-altitude verification.

Full actual build, old/new correction counts and regression evidence: [VALIDATION.md](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/blob/main/VALIDATION.md). No fixture classes, test commands, worlds, native binaries, extra external networking or updater are shipped in the mod. Kaspersky behavior remains unverified; no antivirus exclusion or disabling is required.

## 简体中文

用 **survivalflight-26.2-1.1.2.jar** 替换旧版即可，依赖不变，不要同时安装两个版本。可以直接从旧版升级，无需逐个版本安装。

已复现并修复“冲刺飞行时按 Shift + 前进反复回弹”：防摔着地报告会与服务端原版潜行边缘保护冲突。新版只在受保护的空中 Shift 位置更新前补一次真实未着地状态；保留原位置、输入、原版速度和防摔尝试，也保留正常的服务端纠正机制，不靠忽略传送或限制飞行速度掩盖问题。

包含此前挖掘、防摔与受击悬停修正，三维虚空伤害仍保持原版。云端测试和你的真实局域网验证分别记录，因此继续标为 Experimental / Pre-release；先在低空安全复测，不能保证所有服务端/延迟/断线情况下不回弹或免伤。

## Observed cloud verification / 云端实测

36 state assertions, 25 movement/correction assertions, 16 hurt/projectile/scope checks and 24 mining/landing/void regressions passed. The public 1.1.1 reproduced 4 corrections in the affected combination; the same fixed-version combination recorded 0. Normal sprint-forward control displacement was identical. Repeated Shift transitions, ascent and a protected Shift-forward landing after F7 OFF also recorded 0 corrections.

旧版该组合 4 次纠正，新版实测 0 次；普通冲刺速度对应的位移一致。受击、挖掘、防摔与三维虚空死亡回归保留，真实局域网仍待低空验证。
