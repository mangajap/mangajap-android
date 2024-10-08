package com.tanasi.mangajap.adapters.viewholders

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.tanasi.mangajap.databinding.ItemLibraryStatusBinding
import com.tanasi.mangajap.models.Header

class HeaderViewHolder(
    private val _binding: ViewBinding
) : RecyclerView.ViewHolder(
    _binding.root
) {

    private val context: Context = itemView.context

    private lateinit var header: Header

    fun bind(header: Header) {
        this.header = header
        when (_binding) {
            is ItemLibraryStatusBinding -> displayStatus(_binding)
        }
    }


    private fun displayStatus(binding: ItemLibraryStatusBinding) {
        binding.tvLibraryStatus.text = header.title
    }

}