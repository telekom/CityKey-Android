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
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
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
