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

package com.telekom.citykey.utils

import android.annotation.SuppressLint
import android.content.ContentProviderClient
import android.content.Context
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.exifinterface.media.ExifInterface
import com.telekom.citykey.R
import com.telekom.citykey.domain.city.CityInteractor
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.math.roundToInt
import kotlin.math.sqrt

object BitmapUtil {

    @SuppressLint("Recycle")
    fun getRequiredBitmap(
        galleryUri: Uri?,
        context: Context,
        imageFile: File,
        currentPhotoUri: Uri
    ): Bitmap? {
        val originalUri: Uri
        val rotation: Int

        return try {
            if (galleryUri != null) {
                originalUri = galleryUri
                rotation = getRotationFromGallery(context, originalUri)
            } else {
                originalUri = currentPhotoUri
                rotation = getRotationFromCamera(imageFile)
            }
            val providerClient: ContentProviderClient =
                context.contentResolver.acquireContentProviderClient(originalUri)!!
            val descriptor = providerClient.openFile(originalUri, "r")
            val bitmap = BitmapFactory.decodeFileDescriptor(descriptor?.fileDescriptor)
            descriptor?.close()
            providerClient.close()
            bitmap.rotateImage(rotation.toFloat()).reduceBitmapSize()
        } catch (e: Exception) {
            null
        }
    }

    private fun getRotationFromGallery(context: Context, imageUri: Uri): Int {
        var result = 0
        val columns = arrayOf("orientation")

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(imageUri, columns, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val orientationColumnIndex = cursor.getColumnIndex(columns[0])
                result = cursor.getInt(orientationColumnIndex)
            }
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            cursor?.close()
        }
        return result
    }

    @Throws(IOException::class, SecurityException::class)
    fun getRotationFromCamera(imageFile: File): Int {
        return try {
            val ei = FileInputStream(imageFile).use { ExifInterface(it) }
            val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun Bitmap.rotateImage(degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
    }

    private fun Bitmap.reduceBitmapSize(): Bitmap {
        val ratioSquare: Double
        val bitmapHeight: Int = this.height
        val bitmapWidth: Int = this.width
        ratioSquare = (bitmapHeight * bitmapWidth / 1440000).toDouble()
        if (ratioSquare <= 1) return this
        val ratio = sqrt(ratioSquare)
        val requiredHeight = (bitmapHeight / ratio).roundToInt()
        val requiredWidth = (bitmapWidth / ratio).roundToInt()
        return Bitmap.createScaledBitmap(this, requiredWidth, requiredHeight, true)
    }

    fun createMarker(context: Context): Bitmap {
        val base = AppCompatResources.getDrawable(context, R.drawable.ic_icon_navigation_location_default)!!.apply {
            colorFilter = PorterDuffColorFilter(CityInteractor.cityColorInt, PorterDuff.Mode.SRC_IN)
        }.toBitmap().copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(base)
        canvas.translate(base.width / 2f, base.height / 3f)
        canvas.drawCircle(
            0.0f, 0.0f, 12.0f,
            Paint().apply {
                color = Color.WHITE
                isAntiAlias = true
            }
        )

        return base
    }
}
