# ---------------------------------------------------------------------------
# ElecToolkit R8 configuration
# ---------------------------------------------------------------------------

# Keep line numbers for readable crash reports, but hide the original file name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Runtime-visible annotations are required by Hilt/Dagger and kotlinx.serialization.
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepattributes Signature,InnerClasses,EnclosingMethod

# ---------------------------------------------------------------------------
# kotlinx.serialization
# Serializer lookup is reflective for the synthetic `Companion.serializer()`
# and `$serializer` members, so they must survive shrinking.
# ---------------------------------------------------------------------------
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}

# ---------------------------------------------------------------------------
# Navigation Compose type-safe routes are @Serializable and resolved by type.
# ---------------------------------------------------------------------------
-keep class com.kemalurekli.electricalcalculator.core.navigation.** { *; }

# ---------------------------------------------------------------------------
# Room
# ---------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# ---------------------------------------------------------------------------
# Retrofit / OkHttp — prepared for the future remote data modules.
# ---------------------------------------------------------------------------
-dontwarn okhttp3.internal.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Coroutines
-dontwarn kotlinx.coroutines.debug.**
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Compose runtime keeps its own rules via consumer files; nothing extra needed.
