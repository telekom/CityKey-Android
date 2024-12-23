package com.telekom.citykey.utils.diffutil_callbacks

import androidx.recyclerview.widget.DiffUtil
import com.telekom.citykey.view.infobox.InfoboxItem

class InfoBoxDiffUtils(
    private val oldInfoBoxContent: List<InfoboxItem>,
    private val newInfoBoxContent: List<InfoboxItem>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldInfoBoxContent[oldItemPosition]::class == newInfoBoxContent[newItemPosition]::class

    override fun getOldListSize() = oldInfoBoxContent.size

    override fun getNewListSize() = newInfoBoxContent.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldInfoBoxContent[oldItemPosition] == newInfoBoxContent[newItemPosition]
}
