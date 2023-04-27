# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\user\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:



-keepattributes Signature
-keepattributes *Annotation*
-keep class androidx.appcompat.widget.** { *; }

-keepdirectories com.example.user.store3c
-keepdirectories com.example.user.store3c/**
-keepdirectories com.google.android.youtube.player
-keepdirectories com.google.android.youtube.player/**
-keeppackagenames com.example.user.store3c.**
-keeppackagenames com.google.android.youtube.player.**

-keep class com.example.user.store3c.** { *; }
-keep class com.example.user.store3c.MainActivity.** { *; }
-keep class com.google.android.youtube.player.YouTubePlayerSupportFragmentX.**{ *; }

-keep class com.example.user.store3c.OrderFormActivity.** { *; }
-keep class kotlin.Metadata
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keep class com.google.android.YouTube.** { *; }
-keep class com.google.android.youtube.** { *; }
-keep class com.google.android.youtube.player.** { *; }
-keep class com.google.android.youtube.player.internal.** { *; }

-libraryjars libs/YouTubeAndroidPlayerApi.jar

-verbose
-printmapping build/outputs/mapping/debug/mapping.txt
-printmapping build/outputs/mapping/release/mapping.txt
-printconfiguration build/outputs/mapping/release/config.txt
-printusage  build/outputs/mapping/release/usage.txt
-printseeds  build/outputs/mapping/release/seeds.txt

#-keep class com.google.android.YouTube.**{ *; }
#-keep class com.google.android.youtube.**{ *; }
#-keep class com.example.user.store3c.YouTubeFragment.**{ *; }
#-keep class com.example.user.store3c.YouTubeFailureRecoveryActivity.**{ *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
