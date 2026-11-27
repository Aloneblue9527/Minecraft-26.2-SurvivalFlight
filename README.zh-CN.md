# SurvivalFlight

[English](README.md) · [下载预发布版本](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/releases) · [云端构建](https://github.com/Aloneblue9527/Minecraft-26.2-SurvivalFlight/actions/workflows/build.yml)

**Minecraft 26.2 / Fabric / Java 25 的轻量客户端建筑飞行辅助。**
仅用于房主许可的私人世界。v1.0.0 为 **Experimental 预发布版**，真实远程多人联机尚未验证。客户端飞行不等于服务端授权飞行，也不提供可靠免坠落伤害。

## 安装与使用

1. 启动器选择 **Minecraft Java 26.2**，安装 **Fabric Loader 0.20.1 或更高版本**。
2. 安装对应 26.2 的 **Fabric API 0.161.0+26.2**。
3. 从 Release 下载 `survivalflight-26.2-1.0.0.jar`，放进**你自己客户端**的 `mods` 文件夹。不要安装 `-sources.jar`。
4. 用 Release 的 `SHA256SUMS` 核对指纹。Windows PowerShell：`Get-FileHash .\survivalflight-26.2-1.0.0.jar -Algorithm SHA256`。
5. 加入获许可的私人世界。房主**不需要安装此 Mod**。

默认关闭。按 **F7** 开启飞行权限，再**双击空格**进入原版创造飞行。WASD 水平移动，空格上升，Shift 下降。再次双击空格停止飞行，或按 F7 关闭功能。可在原版“选项 → 控制 → 按键绑定 → 杂项”中修改快捷键。

移动、悬停和速度交给原版处理；Mod 不修改飞行速度或冲刺处理，不改变碰撞、生命值、饥饿值、物品栏、方块交互或生存建筑权限。没有额外加速。

界面仅用原版 Action Bar，提供简体中文和英文。英文状态为：

- `SurvivalFlight: ON`
- `SurvivalFlight: OFF`
- `Flight disabled — Falling danger!`

空中关闭功能、或观察到意外失去飞行时显示坠落警告。为了保守提示，离地很近也会警告。警告**不等于保护**；断线时无法继续显示消息。

## Anti-Kick 与 NoFall 的实际限制

原实现每隔一段时间强制下沉，并发送虚假的 `onGround=true` 封包、清零客户端坠落距离。它们会干扰悬停，且无法证明服务端免伤或不会踢人，现已移除。本 Mod 不发送自定义移动封包，不清零客户端坠落距离。

已检查原版 26.2 实际字节码：

- 普通单人世界“开放到局域网”使用集成服务器；其继承的 `MinecraftServer.allowFlight()` 返回 true。独立服务器则由 `allow-flight` 配置控制。
- 服务端悬空检查还涉及移动、周围方块及其他状态。独立服务器 `allow-flight=false` 时可能被踢；需要房主通过正常管理方式允许飞行。口头许可本身不能改变服务端设置。
- 服务端只有在**服务端自身**允许该玩家飞行时，才接受客户端报告的飞行状态；客户端设定 `mayfly=true` 不会授予服务端权限。
- 服务端负责计算坠落伤害。`allow-flight=true` 只是关闭悬空踢出检查，**不保证生存模式坠落免伤**。下降、着地、F7 关闭、死亡、切换维度、能力同步、断线或高空重连都可能受伤甚至死亡。
- 网络延迟、移动纠正、其他 Mod 和服务器配置仍可能打断飞行。不能保证绝不被踢，也不能保证杀毒软件绝不误报。

没有通用反作弊绕过、封包伪造 NoFall，也无需房主安装本 Mod。若纯客户端行为不能满足你的世界，可靠的服务端飞行与免伤需要房主配合，超出本项目范围。

## 状态管理

仅在生存模式启用。死亡、重生后的玩家对象变化、世界/维度变化、退出重连和游戏模式变化都会重置，需要重新按 F7。切换上下文时清除排队的按键。

关闭时恢复原来的两个飞行标志。一个很小的客户端 Mixin 在原版应用服务端能力封包**之后**丢弃旧快照并关闭功能，保留服务端刚同步的能力。创造和旁观者模式仍由原版控制。不保存或写回速度及其他能力。与同时修改飞行能力的其他客户端 Mod 的组合尚未验证。

## 低空验收流程

先备份世界，征得房主许可，使用可丢弃的测试世界；贵重物品先存好。仅在安全平地上方 **1–2 格**操作，附近备好水。

1. 加入后确认关闭；F7 开启后确认状态文字、按键可修改、双空格、WASD、升降、悬停、放置和破坏方块。
2. 落地关闭，确认正常生存移动、饥饿、生命与方块交互。
3. 离地一格按 F7，确认坠落警告。不要用无保护的高空坠落测试“免伤”。
4. 测试双空格停止飞行、死亡重生、维度切换、退出重连，以及生存 → 创造 → 旁观者 → 生存。确认重置后关闭，原版创造/旁观者正常。
5. 在房主许可下确认能力同步后关闭；只在可安全着地处延长悬停测试，记录踢出和回拉情况。
6. 实际局域网环境验证前不要进行高空建筑。水、脚手架、摔落保护和备份有助于降低风险，但不使此 Mod 成为可靠 NoFall。

## 云端构建与发布

无需你在本地安装 Gradle、IDE 或 Minecraft 开发环境。在 GitHub 的 **Actions → Build SurvivalFlight → Run workflow** 执行后，下载成功运行的 `SurvivalFlight-26.2-1.0.0` Artifact。

push、pull_request、workflow_dispatch 都会编译、运行回归检查、审查 JAR 结构并计算 SHA-256。固定版本如下：

| 组件 | 版本 |
| --- | --- |
| Minecraft | 26.2 |
| Java | 25 |
| Gradle Wrapper | 9.5.1，校验下载指纹 |
| Fabric Loom | 1.17.21 |
| Fabric Loader | 0.20.1 |
| Fabric API | 0.161.0+26.2 |

26.2 已取消混淆，无需额外 mappings。压缩包使用固定时间戳与排序，便于复现。云端维护命令为 `./gradlew clean build --no-daemon` 和 `python3 tools/verify_jar.py`，产物位于 `build/libs/`。

仅提交标准小型 Gradle Wrapper JAR，不提交游戏文件、构建产物、缓存或大型依赖。推送 `v1.0.0` 标签会先完成同样的检查，再发布 **Experimental 预发布版**，附安装 JAR、源码 JAR、指纹与归档清单。普通构建仅有仓库读取权限；发布任务单独使用写权限。第三方 Actions 固定到提交 SHA。

实际验证结果见 [VALIDATION.md](VALIDATION.md)，源码与构建脚本安全审查见 [SECURITY.md](SECURITY.md)，发布限制见 [RELEASE_NOTES.md](RELEASE_NOTES.md)。官方兼容性资料链接列在[英文 README](README.md#official-compatibility-evidence)。

MIT 开源许可，见 [LICENSE](LICENSE)。非 Minecraft 官方产品。
