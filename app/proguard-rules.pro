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

-dontwarn javax.annotation.**
-keepattributes Signature
-keepattributes Exceptions
-keepattributes SetJavaScriptEnabled
-keepattributes JavascriptInterface
-keepattributes InlinedApi
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.shaded.fasterxml.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.apache.**
-dontwarn org.w3c.dom.**

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.syncodec.graphite.presentation.common.richText.*

-keep class com.syncodec.graphite.database.** { *; }
-keep class com.kedia.ogparser.OpenGraphResult
# Change here com.yourcompany.yourpackage
-keep,includedescriptorclasses class com.syncodec.graphite.**$$serializer { *; }
-keepclassmembers class com.syncodec.graphite.** {
    *** Companion;
}
-keepclasseswithmembers class com.yourcompany.yourpackage.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class com.google.android.gms.maps.model.LatLng { *; }
-keep class com.google.android.gms.internal.** { *; }
-keep class com.revenuecat.purchases.** { *; }

-keep public class org.jsoup.** {
    public *;
}

-keep class com.revenuecat.purchases.** { *; }

-keep class com.google.api.services.drive.** { *;}
#-keep class com.google.api.client.googleapis.json.GoogleJsonError.ErrorInfo
-keep class com.google.api.client.googleapis.json.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep the necessary classes and methods for Google Drive API
-keep class com.google.api.services.drive.** { *; }
-keepclassmembers class com.google.api.services.drive.** { *; }

# Keep the necessary classes and methods for Google OAuth2
-keep class com.google.api.client.googleapis.auth.oauth2.** { *; }
-keepclassmembers class com.google.api.client.googleapis.auth.oauth2.** { *; }

# Keep the necessary classes and methods for Google HTTP Client
-keep class com.google.api.client.http.** { *; }
-keepclassmembers class com.google.api.client.http.** { *; }

# Keep the necessary classes and methods for Google JSON
-keep class com.google.api.client.json.** { *; }
-keepclassmembers class com.google.api.client.json.** { *; }

# Keep the necessary classes and methods for Google Gson
-keep class com.google.gson.** { *; }
-keepclassmembers class com.google.gson.** { *; }

# If you're using Apache HTTP Client instead of Google HTTP Client
-keep class org.apache.http.** { *; }
-keepclassmembers class org.apache.http.** { *; }

#-assumenosideeffects class android.util.Log {
#    public static boolean isLoggable(java.lang.String, int);
#    public static int d(...);
#    public static int e(...);
#    public static int i(...);
#    public static int v(...);
#    public static int w(...);
#    public static int wtf(...);
#}
