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

package com.telekom.citykey.view.services.fahrradparken.existing_reports

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.telekom.citykey.R
import com.telekom.citykey.databinding.FahrradparkenReportDetailsBinding
import com.telekom.citykey.models.fahrradparken.FahrradparkenReport
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.loadStyledHtml
import com.telekom.citykey.utils.extensions.openLink
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.tryParsingColorStringToInt
import com.telekom.citykey.utils.extensions.viewBinding

class FahrradparkenReportDetails(
    private val categoryText: String?,
    private val fahrradparkenReport: FahrradparkenReport,
    private val moreInformationBaseUrl: String?,
    private val dismissListener: (() -> Unit)? = null
) : BottomSheetDialogFragment(R.layout.fahrradparken_report_details) {

    private val binding by viewBinding(FahrradparkenReportDetailsBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AuthDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    fun initViews() {
        with(binding) {
            toolbar.apply {
                title = categoryText
                setNavigationIcon(R.drawable.ic_profile_close)
                setNavigationIconTint(getColor(R.color.onSurface))
                setNavigationOnClickListener { dismiss() }
                navigationContentDescription = getString(R.string.accessibility_btn_close)
            }
            fahrradparkenReport.description?.let {
                descriptionWebView.apply {
                    setBackgroundColor(Color.TRANSPARENT)
                    loadStyledHtml(it)
                }
            }
            serviceRequestId.text = fahrradparkenReport.serviceRequestId.let { if (it.isNullOrBlank()) it else "#$it" }
            fahrradparkenReport.extendedAttributes?.markASpot?.let {
                if (it.statusDescriptiveName.isNullOrBlank()) {
                    reportStatusText.setVisible(false)
                } else {
                    reportStatusText.setVisible(true)
                    reportStatusText.text = it.statusDescriptiveName
                    reportStatusText.setBackgroundColor(tryParsingColorStringToInt(it.statusHex))
                }
            }

            moreInfoLayout.setOnClickListener {
                if (moreInformationBaseUrl.isNullOrBlank() || fahrradparkenReport.serviceRequestId.isNullOrBlank())
                    return@setOnClickListener

                val url = if (moreInformationBaseUrl.endsWith("/"))
                    moreInformationBaseUrl + "requests/" + fahrradparkenReport.serviceRequestId
                else
                    moreInformationBaseUrl + "/requests/" + fahrradparkenReport.serviceRequestId
                openLink(url)
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        dismissListener?.invoke()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.invoke()
    }

}
