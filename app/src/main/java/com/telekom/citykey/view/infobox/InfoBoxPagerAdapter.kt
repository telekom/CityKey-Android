package com.telekom.citykey.view.infobox

import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.custom.AnimateableDecoration
import com.telekom.citykey.databinding.InfoboxListBinding
import com.telekom.citykey.utils.extensions.inflateChild

class InfoBoxPagerAdapter(
    private val allMailsAdapter: InfoBoxAdapter,
    private val unreadMailsAdapter: InfoBoxAdapter
) : RecyclerView.Adapter<InfoBoxPagerAdapter.InfoBoxListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoBoxPagerAdapter.InfoBoxListViewHolder =
        InfoBoxListViewHolder(InfoboxListBinding.bind(parent.inflateChild(R.layout.infobox_list)))

    override fun onBindViewHolder(holder: InfoBoxPagerAdapter.InfoBoxListViewHolder, position: Int) {
        holder.bind(if (position == 0) unreadMailsAdapter else allMailsAdapter)
    }

    override fun getItemCount() = 2

    inner class InfoBoxListViewHolder(private val binding: InfoboxListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(adapter: InfoBoxAdapter) {
            InfoBoxSwipeCallbacks(adapter, binding.root.context).also {
                ItemTouchHelper(it).attachToRecyclerView(binding.root)
            }
            binding.root.adapter = adapter
            binding.root.addItemDecoration(
                AnimateableDecoration(
                    color = binding.root.context.getColor(R.color.separator),
                    width = 4f
                )
            )
        }
    }
}
