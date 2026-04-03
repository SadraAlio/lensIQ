package com.example.lensiq

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lensiq.databinding.ItemSuggestionBinding

class SuggestionsAdapter(
    private var items: List<SuggestionItem>,
    private val onItemClick: ((SuggestionItem) -> Unit)? = null
) : RecyclerView.Adapter<SuggestionsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSuggestionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvSubtitle.text = item.subtitle
        holder.binding.tvEmoji.text = item.emoji
        
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(position * 80L)
            .start()

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount() = items.size

    fun submitList(newList: List<SuggestionItem>) {
        items = newList
        notifyDataSetChanged()
    }
}