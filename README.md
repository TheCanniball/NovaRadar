# Nova Radar 🛰️🌌

**Nova Radar** is a highly polished, futuristic network analyzer and security auditing applet built entirely in Kotlin and modern Jetpack Compose. 

Featuring an advanced glassmorphic dashboard design, interactive radar scan sweep visualizers, a local transaction tracking Room database, and dual-language (English / Persian) capability, the applet operates on top of Material Design 3 and implements **dynamic Gemini gradient themes** for pristine visual style.

**Supported Version**: v1.0.0 (Official Update Release)

---

## 🇮🇷 راهنمای سریع به زبان فارسی

پروژه **Nova Radar** یک ابزار مانیتورینگ و آنالیز شبکه مدرن است که با جت‌پک کامپوز (Jetpack Compose) و زبان کاتلین توسعه یافته است. این پروژه آماده‌ی ایمپورت کامل به نرم‌افزار اندروید استودیو (Android Studio) و پوش‌شدن به گیت‌هاب می‌باشد.

### ویژگی‌های کلیدی جدید در نسخه ۱.۰.۰:
1. **پشتیبانی کامل از زبان‌های فارسی و انگلیسی**: ترجمه پویا، راست‌چین و چپ‌چین اصولی، و تغییر آنی کل رابط کاربری.
2. **تم مالتی‌کالر طرح جمنای در حالت تیره و روشن (New Gemini Light Themes)**: بازطراحی کامل ۴ باکس آماری به صورت تمام‌رنگی مالتی‌کالر شیب‌رنگ (شامل آبی ملایم، سبز نعنایی، قرمز گیلاسی و امبر/عسلی گرم) برای ارائه خوانایی مثال‌زدنی در روز به همراه کنترل کنتراست شدید نوشته‌ها در تم تیره و روشن.
3. **امضای دیجیتال هولوگرافیک اختصاصی (Verified Digital Signature Block)**: ادغام موفقیت‌آمیز ماژول تأیید اصالت بیلد با شناسه الکترونیکی توسعه‌دهنده به همراه کد دسترسی هش SHA-256 و نام برنامه‌نویس رسمی (محمد مهرانی) در پنل درباره ما.
4. **مکانیسم چرخش رادار واقعی (طرح برج مراقبت فرودگاه)**: چرخش فرکانسی پیوسته و مداوم ۳۶۰ درجه به همراه سیستم محو شدگی تدریجی سیگنال هدف (Phosphor Decay) پس از عبور پرتو اسکن که کاملاً یادآور رادارهای کلاسیک اوییشن و ناوبری نظامی است.
5. **رابط کاربری بدون تاخیر (صفحه نمایش ۱۲۰ هرتز بدون لگ)**: بهینه‌سازی شده برای جلوگیری از Garbage Collection اندروید با ذخیره و بازنشانی اشیاء سنگین گرافیکی (مانند Paint و Brushها).
6. **نسخه ۱.۰.۰ رسمی**: آماده‌ی نصب سراسری با کمترین حجم خروجی ممکن.

---

## 🌟 Key Features & Visual Design

-   🎨 **ATC-Style Aviation Radar Sweeps**:
    -   **Continuous Rotation**: Dynamic, realistic 360-degree rotation of the sweep line and trailing gradient wedge is always active (dimmed in idle state, full brightness during active scanning), resembling Air Traffic Control (ATC) radar screens.
    -   **Phosphor Decay Physics**: Target IPs illuminate (blip) instantly with high intensity as the sweep arm passes over them, and fade out smoothly into background opacity depending on the angle difference. 
-   ⚡ **Zero-Lag & High Performance**:
    -   **Reused Objects**: Replaces on-the-fly component allocation inside the rendering loop (`Canvas`) with remembered shared drawing brushes and native `Paint` objects, preventing Android Garbage Collector (GC) pressure and offering stutter-free, buttery smooth 60/120 FPS.
    -   **Minimal APK Footprint**: Extremely clean package layout with no bloated dependencies.
-   🎨 **Gemini Multicolor Themes & Gradients (v1.0.0 Extended)**:
    -   **Gemini Light Multicolor Grids**: The 4 statistics HUD cards are styled with vibrant, eye-pleasing multi-color linear gradients (Soft Sky Blue, Minty Green, Rose Coral, and Warm Amber) preserving maximum legibility and dynamic contrast ratios.
    -   **Gemini Dark**: Solid deep black (`#030303`) background with vibrant tri-color linear gradients (Electric Blue, Royal Purple, and Crimson Red) dynamically drawing sweeps on the scanning radar and accenting titles and action buttons.
    -   **Readability Guards**: Font and typography colors adapt dynamically between dark slate `#0F172A` and brilliant white to guarantee text elements remain super visible on all high-contrast modes.
    -   **Holographic Header Shadows**: Live math-driven shadow shift overlay for tech-branded, futuristic headings (specifically customized for the `NOVA RADAR` main header).
-   🛡️ **Embedded Digital Signature Block**:
    -   An official signing card integrated into the About page showcasing developer identity, build validation tag (VERIFIED), and full SHA-256 secure hash to guarantee distribution integrity.
-   📱 **Strict View & Scroll Bounds**:
    -   **Dynamic Adjustments (Zero overlap)**: Safe, adjusted paddings prevent bottom action bars and buttons (e.g. Scan Start/Stop) from falling under the bottom navigation elements on any screen aspect ratios.
-   💾 **Robust SQLite Persistence**: Implemented with Android Room database under clean architecture DAO patterns for registering secure network logs and activity statistics.
-   🌍 **Immediate Dynamic Localization**: Zero-reboot dynamic switching between English and Persian (including Farsi glyph optimizations, numbers, and mirrored layouts).

---

## 🛠️ Tech Stack & Architecture

-   **Language**: Kotlin
-   **UI Framework**: Jetpack Compose (using Material Design 3)
-   **Architecture Pattern**: MVVM (Model-View-ViewModel) with structured StateFlow unidirectional data flow.
-   **Asynchronous Engine**: Kotlin Coroutines & Flows
-   **Local Storage**: Jetpack Room Persistence
-   **Dependency Versioning**: Centralized catalog (`gradle/libs.versions.toml`)

---

## 🚀 Android Studio Integration & Development Guide

Follow these steps to import, run, and continue developing Nova Radar inside **Android Studio**:

### 1. Prerequisites
-   **Android Studio Jellyfish | Ladybug** (or newer version recommending Kotlin 2.0+ support).
-   **Android SDK Platform 36** (API Level 36).
-   **JDK 17** or newer assigned inside Gradle settings.

### 2. Standard Local Settings Setup
To maintain continuous development safety, standard local files like `local.properties` and `.env` are listed inside `.gitignore` and omitted from version control.
When you import the folder, **Android Studio automatically generates** the correct `local.properties` referencing your system's SDK absolute directories (`sdk.dir`).

If you are executing builds via Gradle tools outside of IDE or using custom keys, you can create a local `.env` file at the project root based on our template:
```bash
# Duplicate our template
cp .env.example .env
```
Assign your local `GEMINI_API_KEY` or custom testing configuration inside the `.env` to leverage AI functionalities if required.

### 3. Importing the Project
1.  Launch **Android Studio**.
2.  Select **File -> Open / Import** (or **Open** from the welcome window).
3.  Navigate to the repository physical directory, select the folder containing `settings.gradle.kts`, and click **OK**.
4.  Allow Gradle to sync automatically. The project utilizes standard Gradle wrapper. It will download dependencies in background.

### 4. Codebase Structure Highlights
-   **`app/src/main/java/com/example/MainActivity.kt`**: Entry path. Initializes window edge-to-edge layouts, state scopes, and provides high-level background gradient configurations.
-   **`app/src/main/java/com/example/ui/screens/Screens.kt`**: Houses all screen UI definitions (`DashboardScreen`, `SettingsScreen`, `LogsScreen`, `AboutScreen`) and customizable custom composables such as `CrumpledGlitchText`.
-   **`app/src/main/java/com/example/ui/viewmodel/NovaRadarViewModel.kt`**: Contains the state machine powering live network scanning loops, Room insertions, UI language, translations, and theme switching.
-   **`app/src/main/java/com/example/ui/theme/`**:
    -   `Theme.kt`: Governs Material 3 dark/light color palettes, focusing on beautiful **Blue-Purple-Red gradients**.
    -   `Type.kt` / `Color.kt`: Controls aesthetic layout specs.
-   **`app/src/main/java/com/example/ui/localization/Localization.kt`**: Dictionary mapping all interactive display values between English and Persian dynamically.

---

## 🧪 Testing and Quality Control

The project possesses unit-testing mechanisms including **Robolectric** tests for Local JVM UI simulation and **Roborazzi** for pixel-comparison Screenshot verification:

```bash
# To compile the project and check for validation warnings
./gradlew compileDebugSources

# To run unit and theme state tests
./gradlew testDebugUnitTest

# To run screenshot verification of localized themes
./gradlew verifyRoborazziDebug
```

---

## 🛰️ Future Expansion Directions

For team members expanding the security scanner:
1.  **Network Socket Binding**: Replace simulation flows with real `java.net.Socket` and `NetworkInterface` classes to capture active local network subnets and probe ping sweeps.
2.  **Multiprofile Configurations**: Extend the Room entity configurations in `com.example.data` to store custom scanning presets and port ranges.
3.  **WLAN Sniffing**: Integrate runtime permissions requesting ACCESS_FINE_LOCATION and network statuses to analyze active Wi-Fi channel spectrums.
