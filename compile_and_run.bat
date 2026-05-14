@echo off
REM 编译脚本 - 使用 UTF-8 编码
cd /d %~dp0

echo 使用 UTF-8 编码编译所有文件...

REM 编译所有 Java 文件
javac -encoding UTF-8 -d . ^
    src/logic/*.java ^
    src/model/*.java ^
    src/utils/*.java ^
    src/ui/*.java ^
    src/app/*.java

if %ERRORLEVEL% EQU 0 (
    echo 编译成功！
    echo 运行程序...
    java -Dfile.encoding=UTF-8 app.Main
) else (
    echo 编译失败
    pause
)
