-keepclassmembers class * {
    @androidx.compose.runtime.internal.StableMarker *;
}

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep class kotlin.Metadata { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
