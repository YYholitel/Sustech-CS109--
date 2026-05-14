#!/bin/bash
# macOS/Linux 编译和运行脚本

cd "$(dirname "$0")"

echo "开始编译所有 Java 文件..."

# 编译所有文件，使用 UTF-8 编码
javac -encoding UTF-8 -d . \
    src/logic/*.java \
    src/model/*.java \
    src/utils/*.java \
    src/ui/*.java \
    src/app/*.java 2>&1

if [ $? -eq 0 ]; then
    echo "编译成功！"
    echo "启动程序..."
    java -Dfile.encoding=UTF-8 app.Main
else
    echo "编译失败，请检查错误信息"
    exit 1
fi
