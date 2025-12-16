# Add project specific ProGuard rules here.

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Gson
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.samcod3.alldebrid.data.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# jUPnP
-keep class org.jupnp.** { *; }
-dontwarn org.jupnp.**
