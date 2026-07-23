<div align="center">
  <p>
    <a href="README_ZH.md"><img alt="简体中文" src="https://img.shields.io/badge/语言-简体中文-c0392b?style=flat-square"></a>
    <a href="README.md"><img alt="English" src="https://img.shields.io/badge/Language-English-6e7781?style=flat-square"></a>
  </p>

  <h1>KlotskiPuzzle</h1>

  <p><strong>Java 22+ Swing 华容道工程化学习项目：多格棋子建模、A* 自动求解与非阻塞动画回放。</strong></p>

  <p>
    <a href="https://github.com/44-99/KlotskiPuzzle/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/44-99/KlotskiPuzzle/actions/workflows/ci.yml/badge.svg"></a>
    <a href="https://github.com/44-99/KlotskiPuzzle/releases/latest"><img alt="GitHub Release" src="https://img.shields.io/github/v/release/44-99/KlotskiPuzzle?display_name=tag"></a>
    <a href="https://openjdk.org/projects/jdk/22/"><img alt="Java 22+" src="https://img.shields.io/badge/Java-22%2B-orange.svg"></a>
    <a href="LICENSE"><img alt="MIT license" src="https://img.shields.io/badge/License-MIT-blue.svg"></a>
    <a href="https://github.com/44-99/KlotskiPuzzle/stargazers"><img alt="GitHub stars" src="https://img.shields.io/github/stars/44-99/KlotskiPuzzle?style=flat"></a>
  </p>

  <p>
    <a href="docs/ARCHITECTURE.md">架构说明</a> ·
    <a href="ROADMAP.md">开发路线</a> ·
    <a href="CHANGELOG.md">更新记录</a> ·
    <a href="CONTRIBUTING_ZH.md">参与贡献</a> ·
    <a href="https://github.com/44-99/KlotskiPuzzle/discussions">讨论区</a>
  </p>

  <img src="docs/assets/demo.gif" width="760" alt="A* 后台搜索与 Swing EDT 动画回放的程序化演示">

  <p><sub>该动图由项目脚本按真实线程与移动模型生成，不含第三方游戏画面。</sub></p>
</div>

KlotskiPuzzle 是一个从 Java GUI 课程项目演进而来的 Java 22+ Swing 华容道参考实现。它主要面向已经掌握 Java 基础、准备完成第一个 GUI 与算法综合项目的学生和初级开发者。

这个仓库的重点不是提供成熟的商业游戏客户端，而是展示如何把多格棋子规则、可玩的桌面界面、A* 搜索、后台任务、动画回放、本地存档和自动化测试组织成一个可运行、可验证、可继续改造的工程。

## 为什么有这个项目

很多 Java 课程示例只提供 Swing 界面或孤立的算法片段，读者仍需要自己解决几个关键问题：

- 一个棋子可能占据 1×1、1×2、2×1 或 2×2 个格子，移动规则比普通迷宫更难统一；
- 玩家操作与 AI 各写一套规则容易出现“界面能走、求解器不能走”的不一致；
- 把 A* 直接放在 Swing 事件线程会导致界面假死，求解完成后还需要安全地回放路径；
- 只有截图和源码压缩包无法证明项目能够构建、测试和继续维护。

KlotskiPuzzle 把这些问题放在同一个项目中解决，并保留清晰的技术边界，适合阅读、实验和重构，而不是直接作为课程作业提交。

## 快速开始：运行 Java Swing 华容道完整项目

### 构建最新源码

- JDK 22 或更高版本；
- Maven 3.9 或更高版本；
- 支持 Swing 的桌面图形环境；

```bash
git clone https://github.com/44-99/KlotskiPuzzle.git
cd KlotskiPuzzle
mvn clean verify
mvn exec:java
```

程序会根据操作系统语言自动选择界面：简体中文系统使用中文，其他系统默认英文。也可以显式指定：

```bash
mvn exec:java -Dexec.args="--lang=zh-CN"
mvn exec:java -Dexec.args="--lang=en"
```

构建完成后也可以使用相同参数直接运行 JAR：

```bash
java -jar target/klotski-puzzle-1.0.0.jar --lang=zh-CN
```

图片和音频作为 classpath 资源打入 JAR，运行时不依赖当前工作目录。

### 下载发布版

[最新 GitHub Release](https://github.com/44-99/KlotskiPuzzle/releases/latest) 提供可执行 JAR。更新后的发布流程还会从下一个版本标签开始提供自带 Java 运行时的 Windows 便携包，普通用户无需安装 Java 或 Maven即可体验。

## 你能从中学到什么

| 核心问题 | 项目中的做法 | 推荐入口 |
|---|---|---|
| 多格棋子如何建模和移动 | 用矩阵表示棋盘，由纯 Java 规则层统一识别棋子、碰撞、边界和胜利状态 | [`BoardRules.java`](src/main/java/model/BoardRules.java) |
| 玩家与 AI 如何共用规则 | 玩家移动和求解器状态扩展都调用 `BoardRules.applyMove` | [`GameController.java`](src/main/java/controller/GameController.java)、[`HuaRongDaoSolver.java`](src/main/java/model/HuaRongDaoSolver.java) |
| 如何实现可控的 A* 搜索 | 使用 `PriorityQueue`、最优已知步数表、父状态回溯、取消检查和状态上限 | [`HuaRongDaoSolver.java`](src/main/java/model/HuaRongDaoSolver.java) |
| 如何避免 Swing 界面卡死 | AI 协调器用 `SwingWorker` 后台搜索、`Swing Timer` 在 EDT 上逐步回放 | [`AiSolveCoordinator.java`](src/main/java/controller/AiSolveCoordinator.java) |
| 如何把课程代码变成可验证工程 | Maven 统一构建，JUnit 5 验证规则与路径，GitHub Actions 使用 Java 22 持续集成 | [`pom.xml`](pom.xml)、[`src/test/java`](src/test/java) |
| 如何处理本地数据边界 | 玩家数据写入用户目录，密码使用 PBKDF2，存档可恢复并采用临时文件替换 | [`data`](src/main/java/data) |

AI 执行链路可以概括为：

```text
棋盘快照 -> AiSolveCoordinator -> SwingWorker -> A* -> Swing Timer -> BoardRules -> 界面回放
```

## 可玩功能

- 5×4 华容道棋盘和三种内置布局；
- 通过游戏内语言菜单、系统语言或 `--lang` 参数选择英文、简体中文界面；
- 键盘、WASD、鼠标及界面方向按钮操作；
- A* 后台求解、搜索进度、取消与动画回放；
- 撤销、重新开始和 180 秒限时挑战；
- 本地玩家、游戏存档、步数榜和时间榜；
- 背景音乐与移动、胜利、失败音效。

## 操作与本地数据

1. 创建本地玩家，或选择游客模式；
2. 选择布局并点击棋子；
3. 使用方向键、WASD 或方向按钮移动；
4. 使用“撤军回防”撤销，“军师献策”启动或停止 AI；
5. 登录玩家可以保存进度，游客数据不会持久化。

玩家、存档和排行榜数据保存在 `${user.home}/.klotski-puzzle/`。这是单机档案系统，不是联网账号服务；Base64 只用于棋盘存档编码，不是加密。

## 代码结构

```text
KlotskiPuzzle/
├── src/main/java/
│   ├── cli/          # 可复制运行的求解器指标报告
│   ├── controller/   # 游戏会话，以及 AI 搜索/回放生命周期
│   ├── data/         # 玩家、榜单与可恢复存档
│   ├── model/        # 棋盘模型、布局、移动规则与 A* 求解器
│   ├── util/         # classpath 资源与背景音乐生命周期
│   └── view/         # Swing 窗口和组件
├── src/test/java/    # JUnit 5 测试
├── resources/original/ # 可再分发的原创运行素材
├── docs/             # 架构说明与项目视觉素材
├── tools/            # 原创图片、GIF、音乐和音效生成脚本
└── pom.xml           # Java 22 Maven 构建
```

源码采用务实的分层设计而非严格 MVC：`BoardRules` 是玩家与 AI 的共同规则边界；存档、音效、AI 生命周期和排行榜弹窗已经从大控制类中拆出。更完整的职责与线程说明见 [架构文档](docs/ARCHITECTURE.md)。

## 验证

```bash
mvn clean verify
```

自动化测试覆盖棋盘完整性、四类棋子的合法与非法移动、内置布局求解与路径回放、取消和状态上限、求解节点语义、本地密码哈希、可恢复存档、排行榜容错以及打包资源。登录背景测试还会防止动态 GIF 意外退化为静态图；最新构建结果以页面顶部的 CI 徽章为准。

三种内置布局的步数、展开状态和已发现状态基线记录在 [求解器基准](docs/SOLVER_BENCHMARKS.md) 中，性能改动应在相同计步规则下比较。

无需修改测试即可打印当前机器上的完整指标：

```bash
mvn -q exec:java -Dexec.mainClass=cli.SolverMetricsReport
```

## 项目边界

- 项目要求 Java 22+，只面向支持 Swing 的桌面环境；
- A* 默认最多保留 250,000 个已发现状态，达到上限会停止并明确提示；“一步”定义为一个棋子平移一格，不宣称覆盖所有计步规则；
- 界面仍使用固定窗口和绝对坐标，小屏幕及高 DPI 适配有限；
- 榜单、玩家和存档都是本地数据，不提供服务端认证或跨设备同步；
- 当前没有真正的自由关卡编辑器，难度窗口提供的是三种经过校验的预设布局。

## 参与贡献

请先阅读 [CONTRIBUTING_ZH.md](CONTRIBUTING_ZH.md) 和公开的 [开发路线](ROADMAP.md)。适合的改进方向包括 GUI 集成测试、响应式布局、可验证的关卡编辑器和更细致的求解器基准。问题请提交到 Issues，开放式想法与使用交流请放到 Discussions。

## 许可证

源代码和仓库内程序化生成的原创素材均采用 [MIT License](LICENSE)。素材生成方式与历史清理说明见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
