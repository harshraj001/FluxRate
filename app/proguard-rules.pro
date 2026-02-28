# Keep line numbers for crash debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- Kotlin ----
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# ---- Retrofit ----
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# ---- OkHttp / Okio ----
-dontwarn okhttp3.**
-dontwarn okio.**

# ---- Moshi ----
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonQualifier interface *

# ---- App data models (critical — used by Moshi reflection) ----
-keep class com.oss.fluxrate.data.** { *; }
-keep class com.oss.fluxrate.ui.screens.ThemePreference { *; }

# ---- Keep ALL enums (Moshi EnumJsonAdapter needs original field names) ----
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    <fields>;
}

# ---- Compose ----
-dontwarn androidx.compose.**