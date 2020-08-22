# for Crashlytics
# https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# call from preference-header
-keep public class * extends net.mm2d.dmsexplorer.view.base.PreferenceFragmentBase
