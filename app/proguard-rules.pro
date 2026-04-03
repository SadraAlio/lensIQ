# Retrofit and Gson - Protect models and serialization
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-keep class com.squareup.retrofit2.** { *; }
-keep interface com.squareup.retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken
-keep class com.google.gson.JsonElement
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ML Kit - Keep all vision and common classes
-keep class com.google.mlkit.** { *; }
-keep interface com.google.mlkit.** { *; }

# CameraX - Keep all camera core and lifecycle classes
-keep class androidx.camera.** { *; }
-keep interface androidx.camera.** { *; }

# App Specific Data Classes - Prevent obfuscation of scan and suggestion models
-keep class com.example.lensiq.SuggestionItem { *; }
-keep class com.example.lensiq.ScanResult { *; }
-keep class com.example.lensiq.ScanResult$Companion { *; }
-keep class com.example.lensiq.SuggestionsAdapter { *; }
-keepclassmembers class com.example.lensiq.SuggestionsAdapter$** { *; }

# Parcelable and Serializable - Ensure data can be passed between activities
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
