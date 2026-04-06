# Supabase and Ktor ProGuard Rules
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature, InnerClasses
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName *;
}

# Preserve DTOs for Supabase decoding
-keep class com.ghostbug.heavyliftsapp.data.remote.dto.** { *; }

# AndroidX Credentials & Google ID
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Material 3 and Compose
-keep class androidx.compose.material3.** { *; }

# Fix R8 missing classes for java.lang.management (often caused by Ktor or other libraries using ManagementFactory)
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
