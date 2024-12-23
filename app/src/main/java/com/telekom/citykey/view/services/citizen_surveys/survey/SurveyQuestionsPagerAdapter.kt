package com.telekom.citykey.view.services.citizen_surveys.survey

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.SurveyQuestionPageBinding
import com.telekom.citykey.models.api.requests.TopicAnswers
import com.telekom.citykey.models.citizen_survey.Question
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.dpToPixel
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.setVisible

class SurveyQuestionsPagerAdapter(
    private val onNextClicked: () -> Unit,
    private val onPrevClicked: () -> Unit,
    private val onDoneClicked: () -> Unit,
    private val surveyName: String,
    private val isPreview: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mapTopicAnswer = mutableMapOf<String, MutableList<TopicAnswers>>()
    var color: Int = 0

    private val items = mutableListOf<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        SurveyQuestionsViewHolder(SurveyQuestionPageBinding.bind(parent.inflateChild(R.layout.survey_question_page)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SurveyQuestionsViewHolder).setQuestion(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<Question>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    private inner class SurveyQuestionsViewHolder(val binding: SurveyQuestionPageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var checkMandatory = mutableListOf<Boolean>()
        fun setQuestion(data: Question) {
            binding.questionHint.text = data.questionHint
            binding.questionHint.setVisible(data.questionHint.isNotEmpty())
            binding.questionHint.setTextColor(color)
            binding.question.text = data.questionText
            binding.pagerPrev.strokeColor = ColorStateList.valueOf(color)
            binding.pagerPrev.setOnClickListener { onPrevClicked() }
            binding.pagerPrev.setTextColor(color)
            binding.surveyTitle.text = surveyName

            if (absoluteAdapterPosition == 0) binding.pagerPrev.setVisible(false) else binding.pagerPrev.setVisible(
                true
            )

            binding.pagerNext.apply {
                if (absoluteAdapterPosition == itemCount - 1) {
                    setBackgroundColor(color)
                    setTextColor(context.getColor(R.color.white))
                    setText(R.string.cs_004_button_done)
                    strokeWidth = 0
                } else {
                    strokeColor = ColorStateList.valueOf(color)
                    strokeWidth = 1.dpToPixel(context)
                    setTextColor(color)
                    setText(R.string.cs_004_button_next)
                    setBackgroundColor(context.getColor(R.color.background))
                }
            }

            binding.topicsList.adapter = SurveyTopicsAdapter(data.topics, color) { hasSelection ->
                if (isPreview and (absoluteAdapterPosition == itemCount - 1)) {
                    binding.pagerNext.disable()
                } else {
                    if (hasSelection) binding.pagerNext.enable() else binding.pagerNext.disable()
                }
            }

            binding.pagerNext.setOnClickListener {
                checkMandatory.clear()
                data.topics.forEachIndexed { index, topic ->
                    val topicAnswers = mutableListOf<TopicAnswers>()

                    val itemViewSurvey = (
                            binding.topicsList.findViewHolderForLayoutPosition(index)
                                    as? SurveyTopicsAdapter.TopicViewHolder
                            )
                    if (itemViewSurvey != null) {
                        topicAnswers.addAll(itemViewSurvey.getAnswers(topic, data.questionId))
                    }
                    mapTopicAnswer[topic.topicOrder + data.questionOrder] = topicAnswers
                    checkMandatory.add(topicAnswers.isNotEmpty())
                }
                if (!checkMandatory.contains(false)) {
                    if (absoluteAdapterPosition == itemCount - 1) {
                        onDoneClicked()
                    } else {
                        onNextClicked()
                    }
                }
            }
        }
    }
}
