package com.example.lensiq

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("suggestions.json")
    suspend fun getSuggestions(): Response<Map<String, List<SuggestionItem>>>
}