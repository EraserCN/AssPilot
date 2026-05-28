# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/google/home/sdk/tools/proguard/proguard-android.txt

# Keep Room entities
-keep class wilddad.oppo.asspilot.db.** { *; }

# Keep API models for Gson
-keep class wilddad.oppo.asspilot.api.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# Markwon
-dontwarn io.noties.markwon.**
