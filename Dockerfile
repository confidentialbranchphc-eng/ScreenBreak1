FROM gradle:8.9-jdk17
WORKDIR /app
COPY . .
RUN gradle assembleDebug --no-daemon
CMD ["echo", "APK generated in app/build/outputs/apk/debug/"]
