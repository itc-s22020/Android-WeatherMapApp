package com.example.weathermapapp.api

import com.example.weathermapapp.model.WeatherModel
import io.ktor.client.call.body

object WeatherApp {
    public suspend fun getWeatherData(q: String): WeatherModel {
        return WeatherApiClient.get(q).body()
    }
}