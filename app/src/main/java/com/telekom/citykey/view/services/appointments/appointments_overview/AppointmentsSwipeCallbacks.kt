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
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
 */

package com.telekom.citykey.view.services.appointments.appointments_overview

import android.content.Context
import android.graphics.*
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.networkinterface.models.appointments.Appointment
import com.telekom.citykey.utils.extensions.dpToPixel
import com.telekom.citykey.utils.extensions.isInPast
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

class AppointmentsSwipeCallbacks(
    private val adapter: AppointmentsAdapter,
    private val context: Context
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT
) {

    companion object {
        private const val SWIPE_LEFT_DIRECTION = 4
        private const val TEXT_SIZE = 12f
        private const val DELETE_COLOR = 0xFFD90000.toInt()
        private const val NA_DELETE_COLOR = "#C8C8C8"
        private const val PADDING_END = 21f
    }

    private val deletePaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = DELETE_COLOR
        }
    }

    private val deletePaintNa: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(NA_DELETE_COLOR)
        }
    }
    private val deleteText: String = context.getString(R.string.b_002_infobox_swiped_btn_delete)
    private val textPaint: Paint by lazy {
        Paint().apply {
            color = Color.WHITE
            textSize = TEXT_SIZE.dpToPixel(context)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            isAntiAlias = true
        }
    }

    @ColorInt
    var actionColor = Color.BLACK

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val appointmentsType = adapter.getItem(viewHolder.bindingAdapterPosition)

        when (direction) {
            SWIPE_LEFT_DIRECTION -> {
                if (appointmentsType.apptStatus == Appointment.STATE_CANCELED || appointmentsType
                    .apptStatus == Appointment.STATE_REJECTED || appointmentsType.endTime.isInPast
                ) {
                    adapter.deleteItem(viewHolder.bindingAdapterPosition)
                } else {
                    adapter.changeItem(viewHolder.bindingAdapterPosition)
                }
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        if (viewHolder.bindingAdapterPosition == -1) return
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        when {
            dX < 0 -> { // Swiping to the left
                val appointmentsType = adapter.getItem(viewHolder.bindingAdapterPosition)
                if (appointmentsType.apptStatus == Appointment.STATE_CANCELED || appointmentsType.apptStatus == Appointment.STATE_REJECTED || appointmentsType.endTime.isInPast) {

                    val leftButton =
                        RectF(
                            dX + itemView.right - PADDING_END.dpToPixel(context),
                            itemView.top.toFloat(),
                            itemView.right.toFloat() + PADDING_END.dpToPixel(context),
                            itemView.bottom.toFloat()
                        )

                    deletePaint.alpha = min(255, abs(dX.roundToInt()))
                    c.drawRect(leftButton, deletePaint)
                    drawText(deleteText, c, leftButton)
                } else {
                    var newDx = dX
                    if (newDx <= dX / 4) {
                        newDx = dX / 4
                    }
                    val leftButton =
                        RectF(
                            newDx + itemView.right.toFloat() - PADDING_END.dpToPixel(context),
                            itemView.top.toFloat(),
                            itemView.right.toFloat() + PADDING_END.dpToPixel(context),
                            itemView.bottom.toFloat()
                        )

                    deletePaintNa.alpha = min(255, abs(newDx.roundToInt()))
                    c.drawRect(leftButton, deletePaintNa)
                    drawText(deleteText, c, leftButton)
                    super.onChildDraw(c, recyclerView, viewHolder, newDx, dY, actionState, isCurrentlyActive)
                }
            }
        }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        val appointmentsType = adapter.getItem(viewHolder.bindingAdapterPosition)
        return if (appointmentsType.apptStatus == Appointment.STATE_CANCELED || appointmentsType.apptStatus == Appointment.STATE_REJECTED || appointmentsType.endTime.isInPast) {
            super.getSwipeThreshold(viewHolder)
        } else {
            0.3f
        }
    }

    private fun drawText(text: String, c: Canvas, button: RectF) {
        val textWidth = textPaint.measureText(text)
        c.drawText(text, button.centerX() - (textWidth / 2), button.centerY() + (TEXT_SIZE / 2), textPaint)
    }
}
