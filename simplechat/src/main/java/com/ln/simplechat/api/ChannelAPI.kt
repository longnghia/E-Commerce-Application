package com.ln.simplechat.api

import com.ln.simplechat.model.TopicData
import com.ln.simplechat.model.TopicNotificationResult
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TopicApiService {

    @Headers("Content-Type: application/json")
    @POST
    suspend fun sendNotification(@Body data: TopicData): TopicNotificationResult
}

val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
val httpClient = OkHttpClient.Builder().addInterceptor(logging)

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(httpClient.build())
    .build()

object TopicAPI {
    val retrofitService: TopicApiService by lazy { retrofit.create(TopicApiService::class.java) }
}