package com.example.lensiq

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class SuggestionsRepository {

    suspend fun getForLabel(label: String, context: Context): List<SuggestionItem> {
        val lowerLabel = label.lowercase()
        
        // 1. Try Retrofit
        try {
            val response = RetrofitClient.instance.getSuggestions()
            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    val match = findMatch(lowerLabel, data)
                    if (match != null) return match
                }
            }
        } catch (e: Exception) {
            // Fallback to local
        }

        // 2. Try Assets
        try {
            context.assets.open("suggestions.json").use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val type = object : TypeToken<Map<String, List<SuggestionItem>>>() {}.type
                val data: Map<String, List<SuggestionItem>> = Gson().fromJson(reader, type)
                
                val match = findMatch(lowerLabel, data)
                if (match != null) return match
            }
        } catch (e: Exception) {
            // Fallback to defaults
        }

        // 3. Defaults
        return listOf(
            SuggestionItem("Premium Selection", "Hand-picked for you", "✨"),
            SuggestionItem("Related Accessories", "Complete the look", "👜"),
            SuggestionItem("Top Rated", "Best in category", "⭐")
        )
    }

    private fun findMatch(label: String, data: Map<String, List<SuggestionItem>>): List<SuggestionItem>? {
        for ((key, value) in data) {
            if (label.contains(key.lowercase()) || key.lowercase().contains(label)) {
                return value
            }
        }
        return data["default"]
    }
}