package com.example.weathermapapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherModel(
    @SerialName("city") val city: City,
    @SerialName("list") val forecast: List<ForecastList>
)

@Serializable
data class City(
    val name: String,
)

//ここから天気予報に必要な情報  <ForecastList>

@Serializable
data class ForecastList(
    val dt_txt: String, //日時
    @SerialName("main") val main: Main,
    @SerialName("weather") val weather: List<Weather>,
    @SerialName("wind") val wind: Wind,
    val pop: Double, //降水確率 1~0
    @SerialName("snow") val snow: Snow? = null,
    @SerialName("rain") val rain: Rain? = null,
)

@Serializable
data class Main(
    val temp: Double, //温度
    val feels_like: Double, //体感温度
    val grnd_level: Int, //地上の大気圧 hPa
    val humidity: Int, //湿度 %
)

@Serializable
data class Weather(
    val description: String, //気象情報
    val icon: String, //icon識別用
)

@Serializable
data class Clouds(
    val all: Int, //雲の割合 %
)

@Serializable
data class Wind(
    val speed: Double, //風速 m/s
    val deg: Int, //風向 度
    val gust: Double, //瞬間風速 m/s
)

@Serializable
data class Snow(
    @SerialName("3h") val snow: Double?
)

@Serializable
data class Rain(
    @SerialName("3h") val rain: Double?
)

