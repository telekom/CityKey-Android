/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

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
