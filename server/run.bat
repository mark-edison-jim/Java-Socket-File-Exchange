@echo off
if "%1"=="" (
    echo Please provide a port number.
    exit /b 1
)
javac *.java
if %errorlevel% neq 0 (
    echo Compilation failed.
    exit /b 1
)
java Server %1