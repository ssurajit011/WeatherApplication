import com.example.weatherapp.HistoricalWeatherResponse
import com.example.weatherapp.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("weather")
    fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>

    @GET("onecall/timemachine")
    fun getHistoricalWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("dt") timestamp: Long,
        @Query("appid") apiKey: String
    ): Call<HistoricalWeatherResponse>
}

