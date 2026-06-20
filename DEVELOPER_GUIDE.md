# Nova Radar 🛰️🌌 - Developer & Build Guide
## راهنمای جامع توسعه‌دهندگان و فرآیند بیلد در اندروید استودیو

This document provides a comprehensive technical overview and step-by-step instructions for importing, developing, testing, and building the **Nova Radar** Android application inside **Android Studio**.

این مستند راهنمای تخصصی و گام‌به‌گام برای برنامه‌نویسان جهت ایمپورت، توسعه، تست و کامپایل پروژه **نوا رادار (Nova Radar)** در محیط رسمی **Android Studio** می‌باشد.

---

## 🛠️ 1. System Requirements & Prerequisites / پیش‌نیازهای سیستم

To compile and execute the project without issues, ensure your development environment satisfies the following conditions:
برای بیلد بدون خطا و روان پروژه، مطمئن شوید محیط توسعه شما مشخصات زیر را دارا می‌باشد:

*   **Android Studio Version**: Android Studio Jellyfish, Ladybug, Koala, Meerkat, or newer.
    *   *اندروید استودیو کوالا یا لیدی‌باگ به بالا پیشنهاد می‌شود.*
*   **Kotlin Compiler**: v2.0.x or newer (configured via Gradle dependencies).
*   **JDK (Java Development Kit)**: **JDK 17** is mandatory. Set this inside Android Studio Gradle settings.
    *   *الزامی بودن استفاده از جاوا نسخه ۱۷ (JDK 17) در تنظیمات گریدل.*
*   **Android SDK Platforms**:
    *   **Compile SDK Version**: `36` (Api 36)
    *   **Target SDK Version**: `36` (Api 36)
    *   **Min SDK Version**: `24` (Android 7.0 Nougat)

---

## 📂 2. Core Architecture & Folder Structure / ساختار معماری و پوشه‌های پروژه

The project is structured under the modern **MVVM (Model-View-ViewModel)** architectural pattern with Room database storage following Clean Architecture principles:
این پروژه با الگوبرداری از ساختار استاندارد گوگل مبتنی بر معماری MVVM و سیستم دیتابیس بومی لایه لایه طراحی شده است:

```text
📁 NovaRadar/
├── 📁 app/                             # Sub-module containing the full Android application source
│   ├── 📄 build.gradle.kts             # Module-level Gradle configuration (versions, SDKs, proguard, etc.)
│   └── 📁 src/
│       └── 📁 main/
│           ├── 📄 AndroidManifest.xml   # Application declarations, permissions, theme settings
│           ├── 📁 java/com/example/     # Kotlin package structure
│           │   ├── 📁 data/            # Local SQLite storage using Android Room
│           │   │   ├── 📁 dao/         # Data Access Objects (DaoClasses.kt)
│           │   │   ├── 📁 database/    # Room DB instantiation (NovaRadarDatabase.kt)
│           │   │   └── 📁 model/       # Data class structures (IpSource, PortConfig, ScanHistory)
│           │   ├── 📁 ui/
│           │   │   ├── 📁 screens/     # UI Views (Screens.kt - Dashboard, Logs, Cloudflare, About)
│           │   │   ├── 📁 theme/       # Design System (Theme.kt, Color.kt, Type.kt)
│           │   │   └── 📁 viewmodel/   # Business Logic (NovaRadarViewModel.kt, Core state machine)
│           │   └── 📄 MainActivity.kt  # App Launcher, Window edge-to-edge configurations, and Pager init.
│           └── 📁 res/                 # Graphics assets, layout styles, launcher icons, fonts.
├── 📁 gradle/                          # Centralized gradle dependency version catalog (libs.versions.toml)
│   └── 📄 libs.versions.toml           # Unified third-party dependency version controller
├── 📄 build.gradle.kts                 # Project-level Gradle entrypoint
├── 📄 settings.gradle.kts              # Module and repository declaration registry
└── 📄 README.md                        # Master user-facing repository portal document
```

---

## 🚀 3. Step-by-Step Android Studio Import Guide / راهنمای ایمپورت گام‌به‌گام

Follow these instructions to load the project perfectly in Android Studio:
برای بارگذاری بدون نقص پروژه در اندروید استودیو مراحل زیر را انجام دهید:

1.  **Clone or Download**: Ensure the full repository is downloaded and extracted on your local computer.
    *   *پروژه را بر روی سیستم محلی خود استخراج کنید.*
2.  **Open Android Studio**: Launch your IDE and select **Open** (or choose **File -> Open / Import** from the top menu bar).
    *   *اندروید استودیو را باز کرده و روی گزینه‌ی Open کلیک کنید.*
3.  **Select Directory**: Navigate to the directory containing **`settings.gradle.kts`** and click **OK**.
    *   *پوشه‌ای را که شامل فایل تنظیمات گریدل طرح kts است، انتخاب نمایید.*
4.  **JDK Verification (CRITICAL)**:
    *   Go to **File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle**.
    *   Check **Gradle JDK** and ensure it is pointed to **JDK 17**. If not, download and assign JDK 17.
    *   *اطمینان حاصل کنید که جاوا نسخه ۱۷ در منوی تنظیمات گریدل فعال باشد.*
5.  **Gradle Auto-Sync**: Let the IDE automatically resolve dependencies. It will compile indices and verify sources in background.
    *   *اجازه دهید همگام‌سازی گریدل به صورت خودکار پایان یابد تا کدهای پروژه لود و آماده‌سازی شوند.*

---

## ⚡ 4. Loading Speed & Runtime Optimizations / بهینه‌سازی سرعت و روان‌سازی عملکرد

The application features advanced optimization mechanics to reduce load latency and prevent performance dips during runtime:
برنامه برای لود آنی و اجرای بدون وقفه در دیوایس‌های مختلف اندرویدی شامل بهینه‌سازی‌های عمیق زیر است:

1.  **Zero Garbage Collection (GC) Pressure**:
    *   Typically, rendering high-frequency animations inside a Jetpack Compose `Canvas` can allocate thousands of dynamic brush and text objects per second, causing Android's Garbage Collector to choke (dropping frame rates to 30 FPS).
    *   *Nova Radar* resolves this by allocating complex mathematical matrices, drawing shaders, and platform `Paint` configurations **once** using Jetpack Compose's **`remember`** blocks.
    *   This guarantees absolute butter-smooth **60/120 FPS high-refresh rate** performance on flagship gaming screens.
2.  **Room Database Async Dispatchers**:
    *   All localized SQL storage and transactional inserts are dispatched explicitly under Kotlin Coroutine dedicated threads (`Dispatchers.IO`), isolating background queries and keeping the main UI thread completely free.
3.  **Localized Static Resources**:
    *   The specialized **Vazirmatn** Iranian font values and adaptive assets are packaged natively into `/res/font` and preloaded directly, entirely removing internet font fetches during splash stages and allowing immediate loading screen transitions.

---

## 📦 5. Building the Signed Production APK / فرآیند خروجی گرفتن برای انتشار تجاری

To output the finalized production APK for installation or app store publishing:
برای خروجی گرفتن نهایی با فرمت رسمی APK جهت نصب روی موبایل یا قرار دادن در مارکت‌های مارکت استور:

### Option A: Using the Android Studio GUI (Graphical Interface) / روش اول: از طریق گرافیک اندروید استودیو
1.  Navigate to the top menu bar: **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**.
    *   *از منوی بالا به مسیر Build -> Build Bundle(s) / APK(s) -> Build APK(s) بروید.*
2.  For publishing, choose **Build -> Generate Signed Bundle / APK...**, select your keystore, and build with **Release** variants.
    *   *برای امضای دیجیتال و بارگذاری در پلی‌استور روش Generate Signed را دنبال کنید.*

### Option B: Using Gradle Terminal Commands / روش دوم: دستورات خط فرمان گریدل
Open the Android Studio Terminal and execute:
ترمینال اندروید استودیو را باز کرده و دستور زیر را بنویسید:

```bash
# Clean the project and execute a full release compilation
./gradlew clean assembleDebug

# For standard release builds
./gradlew assembleRelease
```
The compiled installation files will immediately appear inside your local directory at:
فایل‌های کامپایل شده نهایی در پوشه زیر ذخیره می‌شوند:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🛡️ 6. Troubleshooting & Fixes / رفع اشکالات متداول در حین توسعه

*   **Error: "Unsupported class file major version 61"**
    *   *Solution*: This indicates your Gradle is compiled using a Java version greater or smaller than Java 17. Change your JDK configuration to JDK 17 in Android Studio Preferences.
*   **Error: "Cannot find symbol for BuildConfig"**
    *   *Solution*: Run `Build -> Clean Project` and then `Build -> Rebuild Project` to force the Android Gradle Plugin to auto-generate the `BuildConfig` metadata layer.
*   **Font issues on RTL directions**:
    *   *Solution*: The app utilizes the Iranian Vazirmatn font mapped dynamically via the custom `NovaRadarTheme`. Ensure layouts are encapsulated in the custom `LocalizedLayout` to guarantee symmetrical padding conversions between Persian (RTL) and English (LTR).

---
*Happy Coding!  توسعه‌ی موفقی داشته باشید 🛰️🌈*
