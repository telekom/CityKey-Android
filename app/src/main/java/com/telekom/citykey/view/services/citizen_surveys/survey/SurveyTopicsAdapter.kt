package com.telekom.citykey.view.services.citizen_surveys.survey

import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.SurveyQuestionTopicsBinding
import com.telekom.citykey.models.api.requests.TopicAnswers
import com.telekom.citykey.models.citizen_survey.Topic
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.setVisible

class SurveyTopicsAdapter(
    private val topics: List<Topic>,
    @ColorInt private val color: Int,
    private val selectionListener: (Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectionMap = mutableMapOf<Int, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        TopicViewHolder(SurveyQuestionTopicsBinding.bind(parent.inflateChild(R.layout.survey_question_topics)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? TopicViewHolder)?.setOptions(topics[position])
    }

    override fun getItemCount() = topics.size

    inner class TopicViewHolder(private val binding: SurveyQuestionTopicsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setOptions(topic: Topic) {
            binding.optionView.setupOptions(topic, color)
            binding.optionView.selectionListener = {
                selectionMap[absoluteAdapterPosition] = it
                selectionListener(selectionMap.filter { item -> item.value }.size == topics.size)
            }
            binding.topicTitle.setVisible(topic.topicName.isNotBlank())
            binding.topicTitle.text = topic.topicName

            if (topic.topicDesignType == Topic.DESIGN_FRAMED) binding.card.strokeWidth = 2
        }

        fun getAnswers(topic: Topic, questionId: String): MutableList<TopicAnswers> {
            return binding.optionView.collectAnswers(topic, questionId)
        }
    }
}
