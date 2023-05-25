# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\user\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

#-dontshrink
#-dontoptimize
#-dontobfuscate

#-keep class androidx.annotation.Keep
#-keep @androidx.annotation.Keep class * {*;}
#-keepclasseswithmembers class * {
   # @androidx.annotation.Keep <methods>;
#}
#-keepclasseswithmembers class * {
  #  @androidx.annotation.Keep <fields>;
#}
#-keepclasseswithmembers class * {
 #   @androidx.annotation.Keep <init>(...);
#}

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes Throwable
#-keepattributes  *Exception*, *Throwable*

-keepattributes LineNumberTable, SourceFile, EnclosingMethod, InnerClasses
-renamesourcefileattribute SourceFile

-keep class androidx.appcompat.widget.** { *; }

#-ignorewarnings

#-keep public class * extends java.lang.Exception
#-keep class java.lang.Exception.** { *; }
#-keep class java.lang.RuntimeException.** { *; }
#-keep class java.lang.Throwable.** { *; }

-keepdirectories com.example.user.store3c
-keepdirectories com.example.user.store3c/**
-keepdirectories com.google.android.youtube.player
-keepdirectories com.google.android.youtube.player/**
-keeppackagenames com.example.user.store3c.**
-keeppackagenames com.google.android.youtube.player.**

-keep class com.example.user.store3c.** { *; }
-keep class com.example.user.store3c.MainActivity.** { *; }
-keep class com.example.user.store3c.ProductActivity.** { *; }
-keepnames class com.example.user.store3c.ProductActivity { *; }
-keepclassmembers class com.example.user.store3c.ProductActivity.** { *; }
-keepclassmembernames class com.example.user.store3c.ProductActivity.** { *; }
-keepclasseswithmembers class com.example.user.store3c.ProductActivity.** { *; }
-keepclasseswithmembernames class com.example.user.store3c.ProductActivity.** { *; }
-keep class com.example.user.store3c.ProductActivity {
    protected void onCreate(android.os.Bundle);
    public void onClick(android.view.View);
    public void onBackPressed();
    protected void onDestroy();
}

-keepclasseswithmembers class com.example.user.store3c.OrdertActivity.** { *; }
-keepclasseswithmembernames class com.example.user.store3c.OrdertActivity.** { *; }

-keep class com.google.android.youtube.player.YouTubePlayerSupportFragmentX.**{ *; }

-keep class com.example.user.store3c.OrderFormActivity$Companion
-keep class com.example.user.store3c.OrderFormActivity {
    private java.lang.String menuItem;
    private java.lang.String upMenuItem;
    private java.lang.String searchItem;
    private java.lang.String orderFromFullData;
    private java.lang.String notification_list;
    private android.widget.TextView orderText;
    private com.google.firebase.database.DatabaseReference userRef;
    private android.app.ActivityManager$AppTask preTask;
    void onCreate(android.os.Bundle);
    void onClick(android.view.View);
    void onBackPressed();
}

-keep class kotlin.Metadata { *; }

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.YouTube.** { *; }
-keep class com.google.android.youtube.** { *; }
-keep class com.google.android.youtube.player.** { *; }
-keep class com.google.android.youtube.player.internal.** { *; }

-keep class com.canhub.cropper.** { *; }
-keep class com.android.volley.** { *; }
-dontwarn com.android.volley.**

-keep class kotlinx.coroutines.** { *; }
 -dontwarn kotlinx.coroutines.**

-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

#-keep class android.** { *; }
#-keepnames class android.content.** { *; }
#-keepnames class android.content.Intent.** { *; }
#-keepnames class android.content.ComponentName.** { *; }
#-keepnames class android.content.ContextWrapper.** { *; }
#-keepnames class android.app.** { *; }
#-keepnames class android.app.Instrumentation.** { *; }
#-keepnames class android.app.ContextImpl.** { *; }
#-keepnames class android.app.LoadedApk.** { *; }
#-keepnames class android.app.ActivityManager.** { *; }
#-keepnames class android.app.ActivityThread.** { *; }
#-keepnames class android.app.AppOpsManager.** { *; }
#-keepnames class android.util.** { *; }
#-keepnames class android.util.ContainerHelpers.** { *; }
#-keepnames class android.util.ArrayMap.** { *; }
#-keepnames class android.util.AndroidException.** { *; }
#-keepnames class android.util.ExceptionUtils.** { *; }
#-keepnames class android.net.** { *; }
#-keepnames class android.net.Uri.** { *; }
#-keepnames class android.os.** { *; }
#-keepnames class android.os.Binder.** { *; }
#-keepnames class android.os.Bundle.** { *; }
#-keepnames class android.os.BaseBundle.** { *; }
#-keepnames class android.os.BinderProxy.** { *; }
#-keepnames class android.os.Parcel.** { *; }
#-keepnames class android.os.RemoteException.** { *; }

#-keep class androidx.** { *; }
#-keep class com.android.** { *; }
#-keepnames class com.android.internal.app.MessageSamplingConfig.** { *; }
#-keepnames class com.android.server.wm.** { *; }

#-keep class com.google.android.** { *; }
#-keep class java.** { *; }
#-keepnames class java.lang.** { *; }
#-keep class org.jetbrains.kotlin.** { *; }

-libraryjars libs/YouTubeAndroidPlayerApi.jar


-verbose
-printmapping build/outputs/proguard/debug/mapping/mapping.txt
-printmapping build/outputs/proguard/release/mapping/mapping.txt
-printconfiguration build/outputs/proguard/release/mapping/config.txt
-printusage  build/outputs/proguard/release/mapping/usage.txt
-printseeds  build/outputs/proguard/release/mapping/seeds.txt



# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
