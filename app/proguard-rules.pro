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
-keepattributes Exceptions
-keepattributes Throwable

-keepattributes LineNumberTable, SourceFile, EnclosingMethod, InnerClasses
-renamesourcefileattribute SourceFile

-keep class androidx.appcompat.widget.** { *; }

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
    private <fields>;
    protected void onCreate(android.os.Bundle);
    public void onClick(android.view.View);
    public void onBackPressed();
    protected void onDestroy();
}

-keepclasseswithmembers class com.example.user.store3c.OrdertActivity.** { *; }
-keepclasseswithmembernames class com.example.user.store3c.OrdertActivity.** { *; }
-keep class com.google.android.youtube.player.YouTubePlayerSupportFragmentX.**{ *; }
-keep class com.example.user.store3c.PromotionFirebaseMessagingService.** { *; }
-keepclasseswithmembers class com.example.user.store3c.PromotionFirebaseMessagingService.** { *; }
-keepclasseswithmembernames class com.example.user.store3c.PromotionFirebaseMessagingService.** { *; }
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
-keep class kotlinx.coroutines.** { *; }
-keep class com.crashlytics.** { *; }

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
