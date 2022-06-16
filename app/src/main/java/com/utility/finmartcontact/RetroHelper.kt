package com.utility.finmartcontact


import com.google.gson.GsonBuilder
import com.utility.finmartcontact.core.requestbuilder.LoginRequestBuilder
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object   RetroHelper {

    private val retrofit by lazy {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val gson = GsonBuilder()
            .serializeNulls()
            .setLenient()
            .create()

        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .addInterceptor(interceptor)

            .build()


        Retrofit.Builder()
            .baseUrl(BuildConfig.FINMART_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    val api : LoginRequestBuilder.LoginNetworkService by lazy {

        retrofit.create(LoginRequestBuilder.LoginNetworkService::class.java )
    }


}