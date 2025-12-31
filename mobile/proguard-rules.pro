# https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports
-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

# call from preference-header
-keep public class * extends net.mm2d.dmsexplorer.view.base.PreferenceFragmentBase

-assumenosideeffects public class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
}
