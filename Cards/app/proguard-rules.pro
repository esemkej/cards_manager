####################################
# Keep R8 light (Sketchware-friendly)
####################################

-dontoptimize
-dontpreverify

# Keep useful debug info for crashes
-keepattributes SourceFile,LineNumberTable

# Gson needs these (generics + annotations)
-keepattributes Signature
-keepattributes *Annotation*


####################################
# YOUR APP (this is the important one)
####################################

# Keep all your app code and member names so reflection/JSON won't break.
-keep class com.eas.cards2.** { *; }


####################################
# Warnings: scope them (never global)
####################################

-dontwarn com.google.gson.**
-dontwarn com.google.zxing.**
-dontwarn com.google.android.material.**
-dontwarn androidx.**
-dontwarn kotlin.**