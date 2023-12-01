package com.example.weathermapapp.api

import com.example.weathermapapp.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object WeatherApiClient {
    private const val API_ID = BuildConfig.APP_ID
    private const val WEATHER_INFO_URL = "https://api.openweathermap.org/data/2.5/forecast?lang=ja&units=metric"
    private val client = HttpClient(CIO) {
        engine {
            endpoint {
                connectTimeout = 5000
                requestTimeout = 5000
                socketTimeout = 5000
            }
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                }
            )
        }
    }

    public suspend fun get(q: String) =
        client.get{
            url("$WEATHER_INFO_URL&q=$q&appid=$API_ID")
        }
}