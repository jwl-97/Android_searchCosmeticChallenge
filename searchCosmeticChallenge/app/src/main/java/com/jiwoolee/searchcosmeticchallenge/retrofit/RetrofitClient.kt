package com.jiwoolee.searchcosmeticchallenge.retrofit

import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNTgzOTk2MTQ1LCJqdGkiOiJlMzE0MWYyOGY1MjA0MzVjOWZkYWJmNTIzMDMyYTNlMCIsInVzZXJfaWQiOjUzNH0.bZtTFHKhfSjkN9lOwCEpbFV__6_XZ7lmIbHZTXSkJDA"
    private var instance: Retrofit? = null
    fun getInstance(): Retrofit? {
        if (instance == null) {
            val client =
                OkHttpClient.Builder().addInterceptor { chain ->
                    val newRequest: Request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept", "application/json")
                    .build()
                    chain.proceed(newRequest)
                }.build()

            instance = Retrofit.Builder()
                .baseUrl("https://blb-test.morulabs.com/api/fetch/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }
        return instance
    }
}
