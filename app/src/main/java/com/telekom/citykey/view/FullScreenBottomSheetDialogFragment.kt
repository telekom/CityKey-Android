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

package com.telekom.citykey.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.telekom.citykey.R
import com.telekom.citykey.utils.extensions.shouldPreventContentSharing

abstract class FullScreenBottomSheetDialogFragment(
    @LayoutRes private val layoutId: Int,
    private val containsSensitiveInfo: Boolean = false
) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "FullScreenBottomSheetDialogFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AuthDialogTheme)
    }

    override fun onResume() {
        super.onResume()
        if (containsSensitiveInfo && shouldPreventContentSharing) {
            dialog?.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(layoutId, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val resultDialogFragment = super.onCreateDialog(savedInstanceState)
        setFullHeight(resultDialogFragment)
        return resultDialogFragment
    }

    private fun setFullHeight(dialogFragment: Dialog) {
        dialogFragment.setOnShowListener { dialog ->
            (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.let {
                    val params = it.layoutParams
                    params.height = WindowManager.LayoutParams.MATCH_PARENT
                    it.layoutParams = params
                    BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (containsSensitiveInfo && shouldPreventContentSharing) {
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
