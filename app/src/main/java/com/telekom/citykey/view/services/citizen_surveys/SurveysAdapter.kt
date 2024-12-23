package com.telekom.citykey.view.services.citizen_surveys

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.SurveyOverviewListItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.citizen_survey.Survey
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.isInFuture
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setHtmlText
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.toCalendar
import java.util.concurrent.TimeUnit

class SurveysAdapter(private val isPreview: Boolean, private val onSurveySelected: (Survey) -> Unit) :
    ListAdapter<SurveyListItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<SurveyListItem>() {
            override fun areItemsTheSame(oldItem: SurveyListItem, newItem: SurveyListItem): Boolean =
                oldItem::class == newItem::class

            override fun areContentsTheSame(oldItem: SurveyListItem, newItem: SurveyListItem): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.survey_overview_header_item -> HeaderViewHolder(parent.inflateChild(viewType))
        R.layout.survey_overview_empty_item -> NoSurveysViewHolder(parent.inflateChild(viewType))
        else -> ItemViewHolder(SurveyOverviewListItemBinding.bind(parent.inflateChild(viewType)))
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is SurveyListItem.Header -> R.layout.survey_overview_header_item
        is SurveyListItem.NoRunningSurveys -> R.layout.survey_overview_empty_item
        is SurveyListItem.Item -> R.layout.survey_overview_list_item
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.setup(getItem(position))
            is ItemViewHolder -> holder.setup(getItem(position))
        }
    }

    private inner class ItemViewHolder(private val binding: SurveyOverviewListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setup(item: SurveyListItem) {
            if (item is SurveyListItem.Item) {
                val msDiff: Long =
                    item.survey.endDate.toCalendar().timeInMillis - item.survey.startDate.toCalendar().timeInMillis
                val daysDiff: Long = TimeUnit.MILLISECONDS.toDays(msDiff)
                binding.surveyItem.contentDescription = StringBuilder().apply {
                    append(item.survey.daysLeft.toString())
                    append(binding.root.context.getString(if (item.survey.daysLeft == 1) R.string.cs_002_day_label else R.string.cs_002_days_label))
                    append("\n")
                    append(item.survey.name)
                    append("\n")
                    if (item.survey.isPopular) {
                        append(binding.root.context.getString(R.string.cs_002_favored_list_item_label))
                        append("\n")
                    }
                    if (item.survey.status == Survey.STATUS_COMPLETED) {
                        append(binding.root.context.getString(R.string.accessiblity_survey_completed))
                    }
                }.toString()
                binding.daysProgress.setColor(CityInteractor.cityColorInt)
                binding.daysProgress.setValues(daysDiff.toInt(), item.survey.daysLeft)
                if (item.survey.startDate.isInFuture) {
                    binding.daysLeftText.apply {
                        text = context.getString(R.string.cs_002_00_days_label).padStart(2, '0')
                        contentDescription = context.getString(R.string.cs_002_00_days_label)
                    }
                } else {
                    binding.daysLeftText.text = item.survey.daysLeft.toString().padStart(2, '0')
                    binding.daysLeftText.contentDescription = item.survey.daysLeft.toString()
                }
                binding.surveyTitle.text = item.survey.name
                item.survey.description?.let {
                    binding.surveyDescription.setHtmlText(it)
                }
                binding.root.setOnClickListener {
                    if ((item.survey.daysLeft != 0 && item.survey.startDate.isInFuture.not()) or isPreview) onSurveySelected(
                        item.survey
                    )
                }
                binding.root.setAccessibilityRole(AccessibilityRole.Button)
                binding.popularLabel.setVisible(item.survey.isPopular)
                binding.stateIcon.setColorFilter(CityInteractor.cityColorInt)
                binding.stateIcon.setVisible(item.survey.status == Survey.STATUS_COMPLETED)
                binding.daysLabel.text =
                    binding.root.context.getString(if (item.survey.daysLeft == 1) R.string.cs_002_day_label else R.string.cs_002_days_label)
            }
        }
    }

    private inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setup(item: SurveyListItem) {
            if (item is SurveyListItem.Header && itemView is TextView) {
                itemView.apply {
                    setAccessibilityRole(
                        AccessibilityRole.Heading,
                        context.getString(R.string.accessibility_heading_level_2)
                    )
                    text = context.getString(item.titleResId)
                }
            }
        }
    }

    private inner class NoSurveysViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
