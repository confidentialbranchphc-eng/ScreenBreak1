@echo off
echo Building ScreenBreak Debug APK...
call gradle assembleDebug --no-daemon
echo Done! APK is at app\build\outputs\apk\debug\app-debug.apk
pause
