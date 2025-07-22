# TimeManager

[![Minecraft](https://img.shields.io/badge/minecraft-1.21-green.svg)](https://www.minecraft.net/)
[![Java](https://img.shields.io/badge/java-21-orange.svg)](https://www.oracle.com/java/)
[![Folia](https://img.shields.io/badge/folia-supported-brightgreen.svg)](https://papermc.io/software/folia)

一个允许玩家自由修改自己客户端时间而不影响服务器实际时间的插件。
用来拍照或许很有用！

## 特性

- 🕐 **个人时间管理** - 每个玩家可以独立设置自己的时间
- 🔒 **时间冻结** - 冻结或解冻个人时间
- 🌐 **Folia支持** - 完全兼容Folia服务器

## 安装

1. 下载最新版本的TimeManager插件
2. 将`.jar`文件放入服务器的`plugins`文件夹
3. 重启服务器
4. 编辑`plugins/TimeManager/config.yml`进行配置（可选）

## 使用方法

### 基本命令

主命令：`/ptime`

```
/ptime set <time>     - 设置个人时间
/ptime reset          - 重置个人时间为服务器时间
/ptime freeze         - 冻结个人时间
/ptime unfreeze       - 解冻个人时间
/ptime info           - 查看当前个人时间状态
/ptime help           - 显示帮助信息
```

### 时间格式

时间可以用以下格式指定：

- 数字（游戏刻）：`6000`
- 时间名称：`day`, `night`, `noon`, `midnight`

### 使用示例

```bash
# 设置时间为白天
/ptime set day

# 设置时间为6000游戏刻
/ptime set 6000

# 冻结当前时间
/ptime freeze

# 重置为服务器时间
/ptime reset
```

## 权限

| 权限节点           | 描述                    | 默认值    |
|----------------|-----------------------|--------|
| `ptime.set`    | 允许使用set命令             | `true` |
| `ptime.freeze` | 允许使用freeze/unfreeze命令 | `true` |

## 技术特性

- **API版本**: 1.21
- **Java版本**: 21
- **服务器兼容**: Paper, Spigot, Folia
- **数据包操作**: 使用客户端时间同步包，不影响服务器性能


