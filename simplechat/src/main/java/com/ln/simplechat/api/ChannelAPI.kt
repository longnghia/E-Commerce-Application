package com.ln.simplechat.api

import com.ln.simplechat.api.RetrofitInstance.retrofit
import com.ln.simplechat.model.TopicData
import com.ln.simplechat.model.TopicNotificationResult
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TopicApiService {

    @Headers("Content-Type: application/json")
    @POST(NOTIFICATION_PATH)
    suspend fun sendNotification(@Body data: TopicData): TopicNotificationResult
}

object TopicAPI {
    val retrofitService: TopicApiService by lazy { retrofit.create(TopicApiService::class.java) }
}