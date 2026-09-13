-dontpreverify

# Keep useful debug info for crashes
-keepattributes SourceFile,LineNumberTable

# Gson needs these (generics + annotations)
-keepattributes Signature
-keepattributes *Annotation*


####################################
# YOUR APP (this is the important one)
####################################

# Keep app entry points and custom views that Android may instantiate by name.
-keep public class com.eas.cards2.MainActivity
-keep public class com.eas.cards2.ScannerActivity
-keep public class com.eas.cards2.HsvColorPickerView { public <init>(...); }


####################################
# Warnings: scope them (never global)
####################################

-dontwarn com.google.gson.**
-dontwarn com.google.zxing.**
-dontwarn com.google.android.material.**
-dontwarn androidx.**
-dontwarn javax.annotation.Nullable
-dontwarn kotlin.**
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.OpenSSLProvider
