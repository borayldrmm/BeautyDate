# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# ===============================
# PERFORMANCE OPTIMIZATIONS
# ===============================

# Enable aggressive optimizations
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# ===============================
# FIREBASE OPTIMIZATIONS
# ===============================

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep Firestore model classes
-keepclassmembers class com.borayildirim.beautydate.data.models.** {
    *;
}
-keepclassmembers class com.borayildirim.beautydate.data.remote.models.** {
    *;
}

# ===============================
# JETPACK COMPOSE OPTIMIZATIONS
# ===============================

# Keep Compose classes
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ===============================
# HILT DEPENDENCY INJECTION
# ===============================

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel {
    <init>(...);
}

# ===============================
# ROOM DATABASE OPTIMIZATIONS
# ===============================

# Keep Room classes
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep @androidx.room.Entity class * {
    *;
}
-keep @androidx.room.Dao interface * {
    *;
}

# ===============================
# KOTLINX SERIALIZATION
# ===============================

# Keep serialization classes
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class **$$serializer {
    *;
}
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# ===============================
# COROUTINES OPTIMIZATIONS
# ===============================

# Keep coroutines classes
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ===============================
# SECURITY RULES
# ===============================

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Remove debug prints
-assumenosideeffects class kotlin.io.ConsoleKt {
    public static *** println(...);
}

# ===============================
# GENERAL OPTIMIZATIONS
# ===============================

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep custom exceptions
-keep public class * extends java.lang.Exception