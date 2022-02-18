package awesome.animals.koala.data.network

import android.util.Log
import awesome.animals.koala.util.TAG
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*

object KtorClient {
    private val json = kotlinx.serialization.json.Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    val httpClient = HttpClient(Android) {

        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }

        install(Logging) {
            level = LogLevel.NONE
            logger = object : Logger {
                override fun log(message: String) {
                    Log.i(TAG, "Ktor Log: $message")
                }
            }
        }

        install(HttpTimeout) {
            socketTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            requestTimeoutMillis = 30_000
        }
    }
}
