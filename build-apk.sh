#!/usr/bin/env bash
set -e
echo "Building ScreenBreak Debug APK..."
gradle assembleDebug --no-daemon
echo "Done! APK is at app/build/outputs/apk/debug/app-debug.apk"
