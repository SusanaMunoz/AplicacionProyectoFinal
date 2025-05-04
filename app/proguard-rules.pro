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
# Mantener todas las clases de Firebase
-keep class com.google.firebase.** { *; }

# Mantener todas las clases de tu aplicación
-keep class com.example.tdc.** { *; }

# Mantener los nombres de las clases y métodos de Firebase
-keepclassmembers class com.google.firebase.** {
    public <methods>;
    protected <methods>;
}

# Mantener los nombres de las clases y métodos de tu aplicación
-keepclassmembers class com.example.tdc.** {
    public <methods>;
    protected <methods>;
}
