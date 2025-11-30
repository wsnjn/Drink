#!/bin/bash

echo "🧪 药品提醒应用 - 时间设置功能测试"
echo "=================================="

# 检查APK是否存在
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    echo "✅ APK文件存在: $APK_PATH"
    APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
    echo "📦 APK大小: $APK_SIZE"
else
    echo "❌ APK文件不存在，请先构建应用"
    exit 1
fi

echo ""
echo "🔍 代码检查..."

# 检查关键文件是否存在
FILES=(
    "app/src/main/java/com/example/medicine_reminder/medicine/MedicineListFragment.kt"
    "app/src/main/java/com/example/medicine_reminder/medicine/TimeSelectionAdapter.kt"
    "app/src/main/res/layout/dialog_time_selection.xml"
    "app/src/main/res/layout/item_time_selection.xml"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file 不存在"
    fi
done

echo ""
echo "📋 功能验证清单:"
echo "  □ 时间选择器使用TimePickerDialog"
echo "  □ 支持24小时制 (00:00-23:59)"
echo "  □ 支持30分钟设置 (如08:30)"
echo "  □ 可以添加多个时间点"
echo "  □ 可以删除时间点"
echo "  □ 时间格式为HH:mm"

echo ""
echo "🚀 测试建议:"
echo "1. 安装APK到设备: adb install $APK_PATH"
echo "2. 打开应用，添加新药品"
echo "3. 点击'选择服用时间'"
echo "4. 测试设置08:30, 14:30, 21:30等时间"
echo "5. 验证可以添加/删除多个时间点"

echo ""
echo "📱 手动测试步骤:"
echo "1. 启动应用"
echo "2. 点击右下角'+'按钮"
echo "3. 填写药品信息"
echo "4. 点击'选择服用时间'"
echo "5. 点击'添加时间'"
echo "6. 在TimePickerDialog中选择时间"
echo "7. 验证时间格式和功能"

echo ""
echo "✅ 测试准备完成！"
