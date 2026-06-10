# ── DTO для Gson: сериализуются по рефлексии — не переименовывать поля ──
-keep class com.gospomoshnik.data.remote.** { *; }
-keep class com.gospomoshnik.data.payment.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ── Gson ──────────────────────────────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ── Retrofit / OkHttp ──────────────────────────────────────────────────────
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# ── Room ───────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# ── Hilt ───────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# ── Сохранить доменные модели (используются в Compose state) ───────────────
-keep class com.gospomoshnik.domain.model.** { *; }

# ── Coroutines ─────────────────────────────────────────────────────────────
-dontwarn kotlinx.coroutines.**
