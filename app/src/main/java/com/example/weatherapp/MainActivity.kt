package com.example.weatherapp

import WeatherService
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val apiKey = "30d4741c779ba94c470ca1f63045390a"
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("ClickableViewAccessibility")
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

        binding.rootLayout.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val currentFocus = currentFocus
                if (currentFocus is com.google.android.material.textfield.TextInputEditText) {
                    currentFocus.clearFocus()
                    hideKeyboard(v)
                }
            }
            false
        }

        binding.cityEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.setBackgroundResource(R.drawable.blurredbackground)
            } else {
                v.setBackgroundResource(R.drawable.unblurredbackground)
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun fetchWeather(city: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)
        val call = service.getWeather(city, apiKey)

        call.enqueue(object : Callback<WeatherResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    weatherResponse?.let {
                        val temperature = "%.2f".format(it.main.temp - 273.15)
                        "%.2f".format(it.main.feels_like - 273.15)
                        it.main.pressure
                        val windSpeed = it.wind.speed
                        val weatherInfo = it.weather[0].description

                        binding.temperatureTextView.text = "Temp: $temperature °C"
                        binding.humidityTextView.text = "Humidity: ${it.main.humidity} %"
                        binding.windSpeedTextView.text = "Wind Speed: $windSpeed m/s"
                        binding.weatherInfoTextView.text = "Weather: $weatherInfo"

                        // Fetch historical weather based on current coordinates
                        fetchHistoricalWeather(it.coord.lat, it.coord.lon)
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("WeatherApp", response.message())
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchHistoricalWeather(lat: Double, lon: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        // Clear existing historical data views
        binding.historicalDataLayout.removeAllViews()

        for (i in 0 until 4) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val timestamp = calendar.timeInMillis / 1000

            val call = service.getHistoricalWeather(lat, lon, timestamp, apiKey)
            call.enqueue(object : Callback<HistoricalWeatherResponse> {
                @SuppressLint("SetTextI18n", "MissingInflatedId", "InflateParams")
                override fun onResponse(
                    call: Call<HistoricalWeatherResponse>,
                    response: Response<HistoricalWeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val historicalResponse = response.body()
                        historicalResponse?.let {
                            val inflater = layoutInflater
                            val historicalDataItem =
                                inflater.inflate(R.layout.historical_data_item, null)

                            val dateTextView =
                                historicalDataItem.findViewById<TextView>(R.id.dateTextView)
                            val tempTextView =
                                historicalDataItem.findViewById<TextView>(R.id.tempTextView)
                            val humidityTextView =
                                historicalDataItem.findViewById<TextView>(R.id.humidityTextView)
                            val weatherTextView =
                                historicalDataItem.findViewById<TextView>(R.id.weatherDescTextView)

                            val dateFormat =
                                SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                            dateTextView.text =
                                "Date: ${dateFormat.format(Date(it.current.dt * 1000))}"
                            tempTextView.text =
                                "Temp: %.2f °C".format(it.current.temp - 273.15)
                            humidityTextView.text =
                                "Humidity: ${it.current.humidity} %"
                            weatherTextView.text =
                                "Weather: ${it.current.weather[0].description}"

                            // Add historical data item to the layout
                            binding.historicalDataLayout.addView(historicalDataItem)

                            // Logging to check if data is added
                            Log.d("WeatherApp", "Historical data added: ${dateTextView.text}")
                        }
                    } else {
                        Log.d("WeatherApp", "Error fetching historical data: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<HistoricalWeatherResponse>, t: Throwable) {
                    Log.d("WeatherApp", "Error fetching historical data: ${t.message}")
                }
            })
        }
    }
}
