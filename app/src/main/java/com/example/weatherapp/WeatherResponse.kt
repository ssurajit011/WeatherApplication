
package com.example.weatherapp
data class Sys(
    val country: String // Country code
)
data class WeatherResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main,
    val visibility: Int,
    val wind: Wind,
    val clouds: Clouds,
    val sys: Sys,
    val name: String
)

data class Coord(
    val lat: Double,
    val lon: Double
)

data class Weather(
    val description: String
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

data class Wind(
    val speed: Double,
    val deg: Int
)

data class Clouds(
    val all: Int
)


data class HistoricalWeatherResponse(
    val current: Current
)

data class Current(
    val dt: Long,
    val temp: Double,
    val weather: List<Weather>
) {
    val pressure: Int
        get() {
            TODO()
        }
    val humidity: Int = 0

}






