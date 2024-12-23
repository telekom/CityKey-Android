package com.telekom.citykey.view.infobox

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.InfoboxEmptyItemBinding
import com.telekom.citykey.databinding.InfoboxListItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.user.InfoBoxContent
import com.telekom.citykey.utils.diffutil_callbacks.InfoBoxDiffUtils
import com.telekom.citykey.utils.extensions.*
import java.util.*

class InfoBoxAdapter(
    private val toggleReadListener: (read: Boolean, id: Int) -> Unit,
    private val deleteListener: (id: InfoBoxContent) -> Unit,
    @StringRes private val emptyMsgId: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<InfoboxItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == R.layout.infobox_list_item)
            InfoBoxHolder(InfoboxListItemBinding.bind(parent.inflateChild(R.layout.infobox_list_item)))
        else
            InfoBoxEmptyHolder(InfoboxEmptyItemBinding.bind(parent.inflateChild(R.layout.infobox_empty_item)))

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int =
        if (items[position] is InfoboxItem.Empty) R.layout.infobox_empty_item else R.layout.infobox_list_item

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is InfoBoxHolder -> holder.bind((items[position] as InfoboxItem.Mail).item)
            is InfoBoxEmptyHolder -> holder.bind()
        }
    }

    fun updateData(data: List<InfoboxItem>) {
        val oldList = items.toList()
        items.clear()
        items.addAll(data)

        DiffUtil.calculateDiff(InfoBoxDiffUtils(oldList, items))
            .dispatchUpdatesTo(this)
    }

    fun deleteItem(position: Int) {
        deleteListener((items[position] as InfoboxItem.Mail).item)
    }

    fun getItem(position: Int) = (items[position] as InfoboxItem.Mail).item

    fun toggleItemRead(position: Int) {
        val item = (items[position] as InfoboxItem.Mail).item
        toggleReadListener(item.isRead, item.userInfoId)
    }

    inner class InfoBoxEmptyHolder(private val binding: InfoboxEmptyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.imgInfoBoxEmptyMessage.setText(emptyMsgId)
        }
    }

    private inner class InfoBoxHolder(private val binding: InfoboxListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: InfoBoxContent) {
            binding.layoutItemInfoBox.setAccessibilityRole(AccessibilityRole.Button)
            binding.icon.loadFromOSCA(item.category.icon)
            binding.title.text = item.headline
            binding.description.text = item.description
            binding.category.text = item.category.name
            binding.status.setBackgroundColor(CityInteractor.cityColorInt)
            binding.status.setVisible(!item.isRead, View.INVISIBLE)

            if (item.creationDate.isToday) {
                binding.date.text = item.creationDate.getHoursAndMins()
                    .format(item.creationDate)
            } else {
                val dateAsCalendar = item.creationDate.toCalendar()
                binding.date.text =
                    "${dateAsCalendar.getShortMonthName()} ${dateAsCalendar.get(Calendar.DAY_OF_MONTH)}"
            }

            binding.attachments.setColorFilter(CityInteractor.cityColorInt)
            binding.attachments.setVisible(!item.attachments.isNullOrEmpty())

            binding.root.setOnClickListener {
                if (!item.isRead) {
                    item.isRead = true
                    toggleReadListener(false, item.userInfoId)
                }
                it.findNavController()
                    .navigate(InfoBoxDirections.actionInfoBoxToDetailedInfoBox(item))
            }
        }
    }
}
