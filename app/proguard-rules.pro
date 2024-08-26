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
-keep class com.syncodec.graphite.di.model.TipTapFormatKt
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

-dontwarn com.google.android.gms.auth.api.credentials.Credential$Builder
-dontwarn com.google.android.gms.auth.api.credentials.Credential
-dontwarn com.google.android.gms.auth.api.credentials.CredentialRequest$Builder
-dontwarn com.google.android.gms.auth.api.credentials.CredentialRequest
-dontwarn com.google.android.gms.auth.api.credentials.CredentialRequestResponse
-dontwarn com.google.android.gms.auth.api.credentials.Credentials
-dontwarn com.google.android.gms.auth.api.credentials.CredentialsClient
-dontwarn com.google.android.gms.auth.api.credentials.CredentialsOptions$Builder
-dontwarn com.google.android.gms.auth.api.credentials.CredentialsOptions
-dontwarn com.google.android.gms.auth.api.credentials.HintRequest$Builder
-dontwarn com.google.android.gms.auth.api.credentials.HintRequest

-keep public class org.jsoup.** {
    public *;
}

-keep class com.revenuecat.purchases.** { *; }
