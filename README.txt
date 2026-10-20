ساخت APK:
1) پوشه AllowanceApp را در Android Studio باز کنید (File > Open) و صبر کنید Gradle Sync تمام شود.
2) از منو: Build > Build Bundle(s) / APK(s) > Build APK(s)
3) فایل خروجی: app/build/outputs/apk/debug/app-debug.apk
   این فایل را به گوشی منتقل و نصب کنید (نصب از منابع ناشناس را فعال کنید).

یا از ترمینال (با نصب بودن JDK 17 و Android SDK):
   gradle assembleDebug
