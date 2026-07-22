<p align="right">
  <a href="README.md"><img alt="简体中文" src="https://img.shields.io/badge/语言-简体中文-c0392b?style=flat-square"></a>
  <a href="README_EN.md"><img alt="English" src="https://img.shields.io/badge/Language-English-6e7781?style=flat-square"></a>
</p>

# KlotskiPuzzle

[![CI](https://github.com/44-99/KlotskiPuzzle/actions/workflows/ci.yml/badge.svg)](https://github.com/44-99/KlotskiPuzzle/actions/workflows/ci.yml)
[![Java 22+](https://img.shields.io/badge/Java-22%2B-orange.svg)](https://openjdk.org/projects/jdk/22/)
[![Maven 3.9+](https://img.shields.io/badge/Maven-3.9%2B-C71A36.svg?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License: MIT](https://img.shields.io/badge/Code%20License-MIT-blue.svg)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/44-99/KlotskiPuzzle?style=social)](https://github.com/44-99/KlotskiPuzzle/stargazers)

**基于 Java 22 和 Swing 的经典华容道桌面游戏，包含 A* 自动求解与动画回放。**

项目保留了完整的可玩界面，同时提供统一棋盘规则、撤销与本地存档、限时挑战、排行榜和自动化测试。它更适合作为 Java 桌面开发与搜索算法的学习型工程，而不是已经完成发行和性能调优的商业游戏。

![游戏主界面](resources/assets/游戏界面.png)

[观看 AI 自动求解演示](resources/assets/AI演示.mp4)

## 功能

- 5×4 华容道棋盘和三种内置布局；
- 键盘、WASD、鼠标及界面方向按钮操作；
- A* 后台求解和 Swing Timer 动画回放；
- 撤销、重新开始、180 秒限时挑战；
- 本地玩家、游戏存档、步数榜和时间榜；
- 背景音乐与移动、胜利、失败音效。

## 技术实现

| 部分 | 实现 | 选择原因 |
|---|---|---|
| 运行环境 | Java 22+ | 项目的统一构建与 CI 基线；代码使用了 Java 21 引入的 Sequenced Collections API |
| 桌面界面 | Swing | 原项目就是本地桌面游戏，无需引入额外 UI 框架 |
| 构建 | Maven | 统一编译、测试、资源打包和可执行 JAR 生成 |
| 游戏规则 | `model.BoardRules`、`model.Difficulty` | 玩家操作和求解器共用规则；实际棋局入口校验 5×4 尺寸、棋子形状与数量 |
| 自动求解 | A*、`PriorityQueue`、最优已知步数表、状态上限 | 返回可回放路径、求解状态和搜索指标，避免自定义死局无限占用内存 |
| Swing 并发 | `SwingWorker`、`Swing Timer` | 搜索不阻塞事件线程，动画仍在 EDT 执行 |
| 背景音乐 | 单线程 `BackgroundMusicPlayer` | 串行处理暂停、切歌和自然续播，并关闭旧音频流与 `Clip` |
| 本地数据 | 用户目录、PBKDF2、临时文件替换 | 延续本地玩家功能，避免明文密码和源码目录写入 |
| 测试 | JUnit 5、GitHub Actions | 验证规则、简单求解路径、本地凭据和打包资源 |

源码按 `model`、`controller` 和 `view` 分包，但目前只是较粗的职责划分，并非严格 MVC：控制器仍承担部分音频、存档、榜单和弹窗逻辑。

## 运行

### 环境要求

- JDK 22 或更高版本；
- Maven 3.9 或更高版本；
- 支持 Swing 的桌面图形环境；
- Git LFS，用于检出动态背景素材。

### 从源码启动

```bash
git clone https://github.com/44-99/KlotskiPuzzle.git
cd KlotskiPuzzle
git lfs pull
mvn clean verify
mvn exec:java
```

### 运行构建后的 JAR

```bash
java -jar target/klotski-puzzle-1.0.0-SNAPSHOT.jar
```

图片和音频已作为 classpath 资源打入 JAR，不依赖启动时的当前目录。

## 操作

1. 创建本地玩家，或选择游客模式；
2. 选择布局并点击棋子；
3. 使用方向键、WASD 或方向按钮移动；
4. 使用“撤军回防”撤销，“军师献策”启动或停止 AI；
5. 登录玩家可以保存进度，游客数据不会持久化。

玩家、存档和排行榜数据保存在 `${user.home}/.klotski-puzzle/`。这是本地档案系统，不是联网账号服务。Base64 只用于棋盘存档编码，不是加密。

## 代码结构

```text
KlotskiPuzzle/
├── src/main/java/
│   ├── controller/   # 玩家操作、动画、存档与胜负流程
│   ├── data/         # 本地数据路径与玩家凭据
│   ├── model/        # 棋盘模型、难度、移动规则与 A* 求解器
│   ├── util/         # classpath 资源与背景音乐生命周期
│   └── view/         # Swing 窗口和组件
├── src/test/java/    # JUnit 5 测试
├── resources/        # 图片、音频和演示素材
├── docs/             # 设计记录与文章策划
└── pom.xml            # Java 22 Maven 构建
```

建议从这些文件开始阅读：

- [`model/BoardRules.java`](src/main/java/model/BoardRules.java)：棋子识别、合法移动和胜利判断；
- [`model/HuaRongDaoSolver.java`](src/main/java/model/HuaRongDaoSolver.java)：A* 状态扩展和路径回溯；
- [`controller/GameController.java`](src/main/java/controller/GameController.java)：玩家移动、动画和存档流程；
- [`view/game/ControlPanel.java`](src/main/java/view/game/ControlPanel.java)：AI 后台求解与回放编排；
- [`src/test/java`](src/test/java)：当前自动化测试。

## 测试

```bash
mvn clean verify
```

当前 23 项测试覆盖四类棋子的合法/非法移动、实际棋盘完整性、模型防御性复制、三个内置布局的限时求解与路径回放、求解完成/无解/取消/超限状态、本地密码哈希、排行榜容错和打包资源；登录背景测试还会确认优化后的 GIF 仍包含多帧。CI 使用 Temurin JDK 22。

## 已知限制

- 当前可执行 JAR 约 68.3 MiB；登录页仍播放完整 35 秒动画，但打包版本采用 768×432、8 FPS、64 色的优化 GIF。仓库通过 Git LFS 保留约 308 MiB 的 1080p 原始素材，原文件不重复打入 JAR；
- 三个内置布局已有 5 秒求解回归门槛、路径合法性检查和展开/发现状态指标，但尚未建立峰值内存与最优步数基准；
- AI 推演按钮会显示已发现状态数，默认最多保留 250,000 个状态；达到上限时会停止并给出独立提示，而不是误报为无解；
- Swing 界面使用固定窗口和绝对坐标，小屏幕与高 DPI 适配有限；
- 榜单和存档仍使用本地文本格式，不提供跨设备同步；
- 本地登录只服务单机档案，不应视为服务端身份认证方案；
- 当前音乐来自个人 VIP 离线下载，GIF/视频来自 B 站游戏视频，英雄图片来自浏览器搜索截图；这些来源均未提供再分发授权。公开仓库和二进制 Release 都应先替换素材，详见 [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md)。

## 参与贡献

请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。适合的改进方向包括求解器指标与状态上限、GUI 集成测试、职责拆分、响应式布局和合规素材替换。

## 许可证

源代码采用 [MIT License](LICENSE)。`resources/` 中的素材不自动适用 MIT，详情见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
