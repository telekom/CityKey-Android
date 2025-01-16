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

package com.telekom.citykey.view.services

import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.ProgressButton
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.content.ServiceAction
import com.telekom.citykey.utils.extensions.dpToPixel

class DetailedServiceButtonsAdapter(
    private val actions: List<ServiceAction>,
    private val clickListener: (ServiceAction) -> Unit
) :
    RecyclerView.Adapter<DetailedServiceButtonsAdapter.ButtonViewHolder>() {

    companion object {
        private const val ACTION_DESIGN_FILLED = 1
        private const val ACTION_DESIGN_OUTLINE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder =
        ButtonViewHolder(
            ProgressButton(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    val horizontalMargins = resources.getDimension(R.dimen.global_content_margins).toInt()
                    val verticalMargins = 15.dpToPixel(context)

                    clipChildren = false
                    setMargins(
                        horizontalMargins,
                        verticalMargins,
                        horizontalMargins,
                        verticalMargins
                    )
                    gravity = Gravity.CENTER
                }
            }
        )

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        holder.bind(actions[position])
    }

    override fun getItemCount() = actions.size

    inner class ButtonViewHolder(private val view: ProgressButton) : RecyclerView.ViewHolder(view) {

        fun bind(action: ServiceAction) {
            view.text = action.visibleText
            if (action.buttonDesign == ACTION_DESIGN_OUTLINE) {
                view.setupOutlineStyle(CityInteractor.cityColorInt)
            } else {
                view.setupNormalStyle(CityInteractor.cityColorInt)
            }

            view.setOnClickListener {
                clickListener(action)
            }
        }
    }
}
