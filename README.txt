SCREENBREAK - HOW TO GET THE APK (no Android Studio needed)

1. Create a free account at github.com and make a NEW repository (e.g. "ScreenBreak").
2. Upload ALL files and folders from this zip, including the hidden
   ".github" folder (easiest: install "GitHub Desktop" or use git; the web
   uploader can skip hidden folders).
   If ".github/workflows/build.yml" is missing after upload: in the repo click
   Add file > Create new file, name it  .github/workflows/build.yml  and paste
   the content of that file from this zip.
3. Open the "Actions" tab > "Build APK" > wait ~5 minutes for the green tick.
4. Click the finished run > "Artifacts" > download "ScreenBreak-apk" (a zip containing app-debug.apk).
5. Send app-debug.apk to the phone, install it (allow unknown sources).

ON THE PHONE
- Settings > Apps > ScreenBreak > (three dots) > Allow restricted settings
- Open ScreenBreak > "1) Enable blocking" > switch ScreenBreak ON
- Battery: set ScreenBreak to Unrestricted; enable Autostart on Xiaomi/Oppo/Vivo/Huawei
- Set limit, break, PIN > Save. Test first with 3 min limit / 1 min break.

ALTERNATIVE: Android Studio > File > Open this folder > Build > Build APK(s).

FIRST RUN
On first launch the app opens a setup screen where the user chooses how many
minutes of use before a break, how long the break lasts, and a parent PIN.
Nothing is blocked until this setup is finished.
