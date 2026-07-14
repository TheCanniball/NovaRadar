-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep class kotlin.Metadata { *; }

-dontwarn okhttp3.**
-dontwarn okio.**

-keepclassmembers class * {
    @androidx.compose.runtime.internal.StableMarker *;
}

-keepclassmembers enum * { *; }

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keep class com.novascanner.network.MainActivity
-keep class com.novascanner.network.NovaRadarApp

-keepclassmembers class * extends androidx.compose.runtime.Composable { *; }
