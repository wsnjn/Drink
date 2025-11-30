# 💊 药品提醒应用 (Medicine Reminder)

一个功能完整的Android药品提醒应用，帮助用户管理日常服药计划，提供智能提醒和打卡功能。

## 📱 应用简介

**药品提醒应用**是一个基于Kotlin开发的Android应用，专门为需要定期服药的用户设计。应用提供药品管理、定时提醒、打卡记录等功能，确保用户按时服药并记录服药历史。

## ✨ 主要功能

### 🗂️ 药品管理
- **添加药品**：支持添加多种药品信息
- **时间设置**：为每种药品设置多个服药时间
- **药品列表**：清晰的药品管理界面

### ⏰ 智能提醒
- **定时提醒**：根据设置的服药时间自动提醒
- **多种提醒方式**：通知栏提醒、声音提醒、震动提醒
- **精确闹钟**：使用系统精确闹钟确保准时提醒

### 📊 服药记录
- **打卡功能**：一键记录服药情况
- **历史记录**：查看服药打卡历史
- **统计信息**：显示今日已服药和待服药次数

### 🎯 界面特色
- **三标签设计**：首页、药品管理、通知设置
- **信息分离**：已服药和待服药信息清晰分离
- **简洁操作**：点击展开查看详情，直接打卡

## 🛠️ 技术栈

- **开发语言**：Kotlin
- **目标SDK**：Android API 34
- **最低SDK**：Android API 24
- **架构模式**：MVVM + Fragment
- **依赖管理**：Gradle Kotlin DSL

### 主要依赖库
- `androidx.core:core-ktx` - Android核心库
- `androidx.appcompat:appcompat` - 兼容性支持
- `material` - Material Design组件
- `androidx.constraintlayout` - 约束布局
- `androidx.viewpager2:viewpager2` - 页面切换
- `com.google.code.gson:gson` - JSON序列化
- `androidx.media3:media3-exoplayer` - 音频播放

## 📁 项目结构

```
Android/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/medicine_reminder/
│   │   │   ├── MainActivity.kt              # 主Activity
│   │   │   ├── home/                        # 首页相关
│   │   │   │   ├── HomeFragment.kt
│   │   │   │   ├── TodayMedicinesAdapter.kt
│   │   │   │   └── MedicineTimeItem.kt
│   │   │   ├── medicine/                    # 药品管理
│   │   │   │   ├── MedicineListFragment.kt
│   │   │   │   └── MedicineAdapter.kt
│   │   │   ├── notification/                # 通知设置
│   │   │   │   ├── NotificationSettingsFragment.kt
│   │   │   │   └── NotificationAdapter.kt
│   │   │   ├── service/                     # 后台服务
│   │   │   │   ├── MedicineReminderScheduler.kt
│   │   │   │   ├── MedicineNotificationService.kt
│   │   │   │   ├── MedicineNotificationReceiver.kt
│   │   │   │   └── MedicineAlarmReceiver.kt
│   │   │   └── model/                       # 数据模型
│   │   │       ├── Medicine.kt
│   │   │       └── NotificationSettings.kt
│   │   ├── res/                             # 资源文件
│   │   └── AndroidManifest.xml              # 应用配置
├── build.gradle.kts                         # 项目构建配置
└── settings.gradle.kts                      # 项目设置
```

## 🔧 权限说明

应用需要以下权限来提供完整功能：

- **通知权限**：`POST_NOTIFICATIONS` - 发送通知提醒
- **系统权限**：`WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED` - 确保提醒在重启后继续工作
- **精确闹钟**：`SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM` - 精确时间提醒
- **存储权限**：`READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` - 数据存储
- **网络权限**：`INTERNET`, `ACCESS_NETWORK_STATE` - 网络功能支持

## 🚀 构建和运行

### 环境要求
- Android Studio Giraffe 或更高版本
- JDK 17
- Android SDK 34

### 构建步骤
1. 克隆项目到本地
2. 使用Android Studio打开 `Android` 目录
3. 等待Gradle同步完成
4. 连接Android设备或启动模拟器
5. 点击运行按钮构建并安装应用

### 命令行构建
```bash
cd Android
./gradlew assembleDebug
```

## 📋 使用说明

### 首次使用
1. 打开应用，授予必要的权限
2. 在"药品"标签页添加需要管理的药品
3. 为每种药品设置服药时间
4. 在"通知"标签页配置提醒设置

### 日常使用
- **查看服药状态**：在首页查看今日服药情况
- **接收提醒**：系统会在设定时间发送服药提醒
- **打卡记录**：点击提醒卡片进行服药打卡
- **查看历史**：在首页查看服药记录

## 🎨 界面预览

应用采用三标签设计：
1. **首页** - 显示今日服药状态和提醒信息
2. **药品** - 管理药品列表和服药时间
3. **通知** - 配置提醒设置和通知偏好

## 🔄 更新日志

### 最新功能
- ✅ 已服药和待服药信息分离显示
- ✅ 点击展开查看详情功能
- ✅ 简化的打卡操作流程
- ✅ 精确的定时提醒系统

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个项目！

## 📄 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 📞 联系方式

如有问题或建议，请通过GitHub Issues联系我们。

---

**让服药提醒变得更简单，更智能！** 💊✨
