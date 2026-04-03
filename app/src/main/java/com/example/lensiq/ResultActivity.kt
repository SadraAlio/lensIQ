package com.example.lensiq

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lensiq.MainActivity.Companion.EXTRA_CATEGORY
import com.example.lensiq.MainActivity.Companion.EXTRA_CONFIDENCE
import com.example.lensiq.MainActivity.Companion.EXTRA_LABEL
import com.example.lensiq.databinding.ActivityResultBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private val repository = SuggestionsRepository()
    private lateinit var adapter: SuggestionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val label = intent.getStringExtra(EXTRA_LABEL) ?: "Unknown"
        val confidence = intent.getFloatExtra(EXTRA_CONFIDENCE, 0f)
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "General · Object"

        setupUI(label, confidence, category)
        setupChips()
        setupRecyclerView()
        loadSuggestions(label)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnScanAgain.setOnClickListener { finish() }
    }

    private fun setupUI(label: String, confidence: Float, category: String) {
        binding.tvItemName.text = label
        binding.tvCategory.text = category

        // Use extensions for animations
        binding.progressConfidence.animateTo((confidence * 100).toInt(), 800L)
        
        binding.tvItemName.fadeInWithDelay(100L)
        binding.tvCategory.fadeInWithDelay(180L)
        binding.chipScroll.fadeInWithDelay(260L)
        binding.tvRelatedLabel.fadeInWithDelay(340L)
        
        // Formatting confidence text with animator to sync with bar
        val targetProgress = (confidence * 100).toInt()
        val textAnimator = ValueAnimator.ofInt(0, targetProgress)
        textAnimator.duration = 800
        textAnimator.interpolator = AccelerateDecelerateInterpolator()
        textAnimator.addUpdateListener { animation ->
            binding.tvConfidencePercent.text = "${animation.animatedValue}%"
        }
        textAnimator.start()
        
        binding.tvConfidencePercent.fadeInWithDelay(200L)
    }

    private fun setupChips() {
        val chipColors = listOf("#E6F1FB", "#FAEEDA", "#EAF3DE", "#F3E6FB")
        val chips = listOf(binding.chip1, binding.chip2, binding.chip3, binding.chip4)
        
        chips.forEachIndexed { index, chip ->
            chip.setChipBackgroundColorResource(android.R.color.transparent)
            chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.parseColor(chipColors[index % chipColors.size]))
        }
    }

    private fun setupRecyclerView() {
        adapter = SuggestionsAdapter(emptyList())
        binding.rvSuggestions.layoutManager = LinearLayoutManager(this)
        binding.rvSuggestions.adapter = adapter
    }

    private fun loadSuggestions(label: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val suggestions = repository.getForLabel(label, this@ResultActivity)
            withContext(Dispatchers.Main) {
                adapter.submitList(suggestions)
            }
        }
    }
}