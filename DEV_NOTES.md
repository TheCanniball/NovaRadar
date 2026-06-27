# v1.5.1 — نکات فنی

## علت ارور در نسخه‌های قبل
- `minOf` یک تابع استاندارد Kotlin است که در برخی محیط‌های بیلد (مخصوصاً JDK 21 + Gradle 9.x) به درستی رزولوشن نمی‌شود و باعث `Unresolved reference` در زمان کامپایل می‌گردد.  
**رفع**: جایگزین با `.coerceAtMost()` — تابع extension استاندارد Kotlin که نیاز به import ندارد و در همه نسخه‌ها کار می‌کند.

## تغییرات این نسخه

### ۱. SNI سفارشی (TLS Handshake)
- اضافه شدن `vlessSNI = "nova2.altramax083.workers.dev"` به `NovaRadarViewModel.kt`
- در `deepTestConnect`، قبل از startHandshake، `SSLParameters.setServerNames()` مقدار `SNIHostName(vlessSNI)` را ست می‌کند
- منطبق با نسخه Go اصلی IRNova/NovaRadar که از `tls.Config{ServerName: vlessSNI}` استفاده می‌کند
- بر خلاف custom TrustManager (که Play Protect آن را به عنوان بدافزار تشخیص می‌دهد)، `SNIHostName` یک API استاندارد Java است

### ۲. بازسازی منابع IP به ۳ گروه
- **کلودفلر** (45 رنج CIDR) — ترکیب 15 رنج رسمی + رنج‌های اضافه کاربر
- **آکامای** (6 رنج CIDR)
- **ورسل** (14 رنج CIDR)
- فقط کلودفلر به صورت پیش‌فرض فعال است
- پشتیبانی از چندین CIDR در یک IpSource با جداکننده `,`
- تابع `generateIpsForSubnet()` به صورت fair sampling از همه CIDRها نمونه می‌گیرد

### ۳. تغییرات ظاهری رادار
- حذف حلقه سوم (بیرونی) رادار
- حلقه دوم به عنوان بُردر اصلی با stroke 2.5f
- خطوط کراس‌هیر و sweep arm تنها تا حلقه دوم رسم می‌شوند
- نقطه‌های IPها درون حلقه دوم محدود شده‌اند

### ۴. فیکس CI
- مسیر keystore در GitHub Actions فیکس شد: `${{ github.workspace }}/nova-radar-key.jks`
- این مسیر با `build.gradle.kts` (که از `${rootDir}/nova-radar-key.jks` استفاده می‌کند) هماهنگ است

### ۵. ارتقاء دیتابیس
- نسخه دیتابیس: 3 → 4
- `fallbackToDestructiveMigration()` — نصب جدید دیتابیس تمیز دارد، کاربران قبلی دیتابیسشان پاک می‌شود

## وضعیت بیلد
- **APK لوکال**: ✅ `NovaRadar-v1.5.1-arm64-v8a-release.apk`
- **GitHub Actions**: در حال اجرا (گیتهاب در ایران فیلتر است — ممکن است نیاز به فیلترشکن داشته باشید)
- **نصب**: APK ساید‌لود — هشدار Play Protect "Unknown app" طبیعی است (بدون امضای Play Console)

## نکات امنیتی
- هیچ TrustManager سفارشی استفاده نشده — Play Protect "Harmful app" نمی‌دهد
- همه رنج‌های IP از سرویس‌های معتبر (Cloudflare, Akamai, Vercel)
