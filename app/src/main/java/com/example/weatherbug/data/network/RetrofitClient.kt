package com.example.weatherbug.data.network


import com.example.weatherbug.core.util.Constants
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient {


    private val httpLoggingInterceptor = HttpLoggingInterceptor { message ->

    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }



    private val appLoggerInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request()



        val startMs  = System.currentTimeMillis()
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {

            throw e
        }
        val durationMs = System.currentTimeMillis() - startMs

        val responseBody    = response.body
        val responseBodyStr = responseBody?.string()



        response.newBuilder()
            .body(
                okhttp3.ResponseBody.create(
                    responseBody?.contentType(),
                    responseBodyStr ?: ""
                )
            )
            .build()
    }


    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(appLoggerInterceptor)
        .addInterceptor(httpLoggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()


    private val gson = GsonBuilder()
        .setLenient()
        .create()



    private val apiRetrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()


    private val proRetrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL_PRO)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()



    val apiService: WeatherApiService by lazy {
        apiRetrofit.create(WeatherApiService::class.java)
    }


    val proService: WeatherApiService by lazy {
        proRetrofit.create(WeatherApiService::class.java)
    }
}