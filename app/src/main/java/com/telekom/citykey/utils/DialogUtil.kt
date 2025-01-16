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

package com.telekom.citykey.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telekom.citykey.R
import com.telekom.citykey.utils.extensions.startActivity
import com.telekom.citykey.view.user.login.LoginActivity
import java.util.*

object DialogUtil {

    fun showInfoDialog(
        context: Context,
        @StringRes title: Int,
        @StringRes message: Int,
        okBtnClickListener: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogMaterialTheme)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> okBtnClickListener?.invoke() }
            .show()
    }

    fun showTechnicalError(context: Context) {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogMaterialTheme)
            .setTitle(R.string.dialog_technical_error_title)
            .setMessage(R.string.dialog_technical_error_message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    fun showInfoDialog(context: Context, @StringRes message: Int) {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogMaterialTheme)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    fun showDialog(
        context: Context,
        message: String,
        @StringRes buttonLabel: Int,
        listener: (() -> Unit)
    ) {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogMaterialTheme)
            .setMessage(message)
            .setPositiveButton(buttonLabel) { _, _ -> listener.invoke() }
            .setCancelable(false)
            .show()
    }

    /**
     * Display a dialog with positive and negative button
     */
    fun showDialogPositiveNegative(
        context: Context,
        @StringRes title: Int? = null,
        @StringRes message: Int,
        @StringRes positiveBtnLabel: Int,
        @StringRes negativeBtnLabel: Int,
        positiveClickListener: (() -> Unit),
        negativeClickListener: (() -> Unit)? = null,
        isCancelable: Boolean = true
    ) {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogMaterialTheme)
            .apply { title?.let(::setTitle) }
            .setMessage(message)
            .setCancelable(isCancelable)
            .setPositiveButton(positiveBtnLabel) { dialog, _ -> positiveClickListener.invoke(); dialog.dismiss() }
            .setNegativeButton(negativeBtnLabel) { dialog, _ -> negativeClickListener?.invoke(); dialog.dismiss() }
            .show()
    }

    /**
     * Display a dialog that login is required
     */
    fun showLoginRequired(
        context: Context,
        cancelable: Boolean = false,
        positiveClickListener: (() -> Unit) = { context.startActivity<LoginActivity>() },
        negativeClickListener: (() -> Unit)? = null
    ): AlertDialog =
        MaterialAlertDialogBuilder(context, R.style.AlertDialogMaterialTheme)
            .setTitle(R.string.dialog_login_required_title)
            .setMessage(R.string.dialog_login_required_message)
            .setCancelable(cancelable)
            .setPositiveButton(R.string.dialog_login_required_btn_login) { dialog, _ -> positiveClickListener.invoke(); dialog.dismiss() }
            .setNegativeButton(R.string.dialog_login_required_btn_later) { dialog, _ -> negativeClickListener?.invoke(); dialog.dismiss() }
            .show()

    fun showRetryDialog(
        context: Context,
        onRetry: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ): AlertDialog = MaterialAlertDialogBuilder(context, R.style.AlertDialogMaterialTheme)
        .setTitle(R.string.dialog_retry_title)
        .setMessage(R.string.dialog_retry_description)
        .setPositiveButton(R.string.dialog_retry_retry_button) { dialog, _ -> onRetry?.invoke(); dialog.dismiss() }
        .setCancelable(false)
        .setNegativeButton(android.R.string.cancel) { dialog, _ -> onCancel?.invoke(); dialog.dismiss() }
        .show()

    fun showNoInternetDialog(
        context: Context,
        onCancel: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogMaterialTheme)
            .setTitle(R.string.dialog_retry_title)
            .setMessage(R.string.dialog_no_internet)
            .setCancelable(false)
            .setNegativeButton(android.R.string.cancel) { _, _ -> onCancel?.invoke() }
            .show()
    }

    fun showNoLocationServiceDialog(
        context: Context,
        positiveClickListener: () -> Unit,
        cancelClickListener: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogMaterialTheme)
            .setTitle(R.string.c_001_cities_cannot_access_location_dialog_title)
            .setMessage(R.string.c_001_cities_gps_turned_off)
            .setCancelable(true)
            .setPositiveButton(R.string.c_001_cities_cannot_access_location_btn_poitive) { dialog, _ -> positiveClickListener.invoke(); dialog.dismiss() }
            .setOnCancelListener { dialog -> cancelClickListener?.invoke(); dialog.dismiss() }
            .show()
    }

    /**
     * Ask user to turn GPS on
     */
    fun showDialogLocationPermissionRequest(
        context: Context,
        positiveClickListener: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogMaterialTheme)
            .setTitle(context.getString(R.string.c_001_cities_permission_dialog_title))
            .setMessage(context.getString(R.string.c_001_cities_permission_dialog_content))
            .setPositiveButton(
                context.getString(R.string.c_001_cities_permission_dialog_btn_positive)
            ) { _, _ -> positiveClickListener?.invoke() }
            .setNegativeButton(context.getString(R.string.c_001_cities_permission_dialog_btn_negative), null)
            .show()
    }

    fun showCityNoMoreActive(
        context: Context,
        positiveClickListener: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogMaterialTheme)
            .setTitle(R.string.city_not_available_dialog_title)
            .setMessage(R.string.city_not_available_dialog_body)
            .setCancelable(false)
            .setPositiveButton(
                R.string.c_001_cities_permission_dialog_btn_positive
            ) { _, _ -> positiveClickListener?.invoke() }
            .show()
    }

    fun showCancelProcessDialog(
        context: Context,
        onCancel: (() -> Unit)
    ) {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogMaterialTheme)
            .setTitle(R.string.egov_cancel_workflow_dialog_title)
            .setMessage(R.string.egov_cancel_workflow_dialog_message)
            .setPositiveButton(R.string.egov_cancel_workflow_dialog_ok_button) { dialog, _ -> onCancel(); dialog.dismiss() }
            .setNegativeButton(R.string.egov_cancel_workflow_dialog_cancel_button) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun showDatePickerDialog(
        fragmentManager: FragmentManager,
        onDateSelected: (Date) -> Unit,
        onCancel: () -> Unit
    ) {
        MaterialDatePicker.Builder.datePicker()
            .setTheme(R.style.ThemeOverlay_App_DatePicker)
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setEnd(System.currentTimeMillis())
                    .build()
            )
            .build()
            .apply {
                addOnPositiveButtonClickListener { onDateSelected(Date(it)) }
                addOnNegativeButtonClickListener { onCancel() }
                show(fragmentManager, null)
            }
    }
}
