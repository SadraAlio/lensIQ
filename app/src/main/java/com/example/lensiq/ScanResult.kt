package com.example.lensiq

import java.util.*

data class ScanResult(
    val label: String,
    val confidence: Float,
    val category: String
) {
    companion object {
        fun fromLabel(label: String, confidence: Float): ScanResult {
            val lowerLabel = label.lowercase(Locale.ROOT)
            val category = when {
                lowerLabel.contains("shirt") || lowerLabel.contains("top") || lowerLabel.contains("blouse") -> "Apparel · Tops"
                lowerLabel.contains("pants") || lowerLabel.contains("jeans") || lowerLabel.contains("trousers") -> "Apparel · Bottoms"
                lowerLabel.contains("shoe") || lowerLabel.contains("boot") || lowerLabel.contains("sneaker") -> "Footwear"
                lowerLabel.contains("jacket") || lowerLabel.contains("coat") || lowerLabel.contains("blazer") -> "Apparel · Outerwear"
                lowerLabel.contains("watch") || lowerLabel.contains("clock") -> "Accessories"
                lowerLabel.contains("bottle") || lowerLabel.contains("cup") || lowerLabel.contains("mug") -> "Drinkware"
                lowerLabel.contains("book") || lowerLabel.contains("magazine") -> "Media"
                lowerLabel.contains("phone") || lowerLabel.contains("device") || lowerLabel.contains("screen") -> "Electronics"
                lowerLabel.contains("bag") || lowerLabel.contains("backpack") || lowerLabel.contains("purse") -> "Bags"
                lowerLabel.contains("food") || lowerLabel.contains("fruit") || lowerLabel.contains("vegetable") -> "Food · Grocery"
                else -> "General · Object"
            }
            return ScanResult(label, confidence, category)
        }
    }
}