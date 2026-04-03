package com.example.lensiq

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

class ImageClassifier {

    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableClassification()
            .enableMultipleObjects()
            .build()
    )

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.35f) // Lowered to see more candidates for smart filtering
            .build()
    )

    // Keywords that are too generic and often lead to wrong "first guesses"
    private val genericKeywords = hashSetOf(
        "Rectangle", "Parallel", "Property", "Font", "Line", "Material", 
        "Pattern", "Symmetry", "Angle", "Joint", "Wood", "Metal", "Product",
        "Hand", "Finger" // Feet are often misidentified as hands; we'll look for "Foot" instead
    )

    @OptIn(ExperimentalGetImage::class)
    fun classify(
        imageProxy: ImageProxy,
        onResult: (ScanResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        objectDetector.process(inputImage)
            .addOnSuccessListener { detectedObjects ->
                labeler.process(inputImage)
                    .addOnSuccessListener { labels ->
                        // Strategy: Look for the most specific label in the top results
                        // that isn't on our "generic/noisy" list.
                        
                        val bestLabel = labels.firstOrNull { label ->
                            !genericKeywords.any { noise -> 
                                label.text.contains(noise, ignoreCase = true) 
                            }
                        } ?: labels.firstOrNull() // Fallback to best guess if all are "noisy"

                        if (bestLabel != null) {
                            // If we detected a "Home Good" but the labeler says "Musical Instrument" 
                            // for a Laptop, we can try to find a better match in the top 5
                            var finalLabel = bestLabel.text
                            var finalConfidence = bestLabel.confidence

                            // Special check for Laptop/Piano confusion
                            if (finalLabel.contains("Musical", ignoreCase = true)) {
                                val laptopCandidate = labels.find { it.text.contains("Laptop", ignoreCase = true) || it.text.contains("Computer", ignoreCase = true) }
                                if (laptopCandidate != null) {
                                    finalLabel = laptopCandidate.text
                                    finalConfidence = laptopCandidate.confidence
                                }
                            }

                            onResult(ScanResult.fromLabel(finalLabel, finalConfidence))
                        } else {
                            onResult(ScanResult("Unknown", 0f, "General · Object"))
                        }
                        imageProxy.close()
                    }
                    .addOnFailureListener { e ->
                        onError(e)
                        imageProxy.close()
                    }
            }
            .addOnFailureListener { e ->
                onError(e)
                imageProxy.close()
            }
    }
}