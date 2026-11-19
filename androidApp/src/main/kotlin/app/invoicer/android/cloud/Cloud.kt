package app.invoicer.android.cloud

import app.invoicer.android.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

/** Optional cloud backend. [client] is null when no Supabase credentials were configured at build time. */
@OptIn(ExperimentalSerializationApi::class)
object Cloud {
    val client: SupabaseClient? by lazy {
        if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_KEY.isBlank()) {
            null
        } else {
            createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) {
                defaultSerializer = KotlinXSerializer(Json {
                    namingStrategy = JsonNamingStrategy.SnakeCase
                    ignoreUnknownKeys = true
                })
                install(Auth)
                install(Postgrest)
            }
        }
    }
}
