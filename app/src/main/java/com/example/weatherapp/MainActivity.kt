package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private val apiKey = "30d4741c779ba94c470ca1f63045390a"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in)

        binding.fetchWeatherButton.setOnClickListener {
            val city = binding.cityEditText.text.toString()
            if (city.isNotEmpty()) {
                fetchWeather(city)
                binding.weatherTextView.startAnimation(fadeInAnimation)
                binding.fetchWeatherButton.startAnimation(slideInAnimation)
            } else {
                Toast.makeText(this, "Please enter a city", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchWeather(city: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)
        val call = service.getWeather(city, apiKey)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    weatherResponse?.let {
                        val weatherInfo = """
                            City: ${it.name}, ${it.sys.country}
                            Temperature: ${it.main.temp - 273.15} °C
                            Feels Like: ${it.main.feels_like - 273.15} °C
                            Min Temperature: ${it.main.temp_min - 273.15} °C
                            Max Temperature: ${it.main.temp_max - 273.15} °C
                            Pressure: ${it.main.pressure} hPa
                            Humidity: ${it.main.humidity} %
                            Weather: ${it.weather[0].description}
                            Visibility: ${it.visibility} meters
                            Wind Speed: ${it.wind.speed} m/s
                            Wind Direction: ${it.wind.deg}°
                            Cloudiness: ${it.clouds.all} %
                            Sunrise: ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH).format(java.util.Date(it.sys.sunrise * 1000))}
                            Sunset: ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH).format(java.util.Date(it.sys.sunset * 1000))}
                        """.trimIndent()

                        binding.weatherTextView.text = weatherInfo
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    Log.d("Heda", response.message())
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
