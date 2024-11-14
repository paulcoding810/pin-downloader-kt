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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-obfuscationdictionary dict.txt
-classobfuscationdictionary dict.txt
-packageobfuscationdictionary dict.txt

# Google Mobile Ads SDK (AdMob)
-keep public class com.google.android.gms.ads.** {
    public *;
}

-keepclassmembers class * extends com.google.android.gms.ads.AdListener {
    public void *(...);
}

# Keep track of Parcelable implementations (needed by Google Play Services)
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Needed for R8
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**