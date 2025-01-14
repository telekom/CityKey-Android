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

package com.telekom.citykey.common

import android.content.Context
import android.os.Environment
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class FileLoggingTree(private val context: Context) : DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)
        try {
            val path = "Log"
            val fileNameTimeStamp: String = SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            ).format(Date())
            val logTimeStamp: String = SimpleDateFormat(
                "E MMM dd yyyy 'at' hh:mm:ss:SSS aaa",
                Locale.getDefault()
            ).format(Date())
            val fileName = "$fileNameTimeStamp.html"

            // Create file
            val file: File? = generateFile(path, fileName)

            // If file created or exists save logs
            if (file != null) {
                val writer = FileWriter(file, true)
                writer
                    .append("<p style=\"font-family: monospace; background: #f5f5f5; padding: 5px 0px; margin:10px 0px;\"> ")
                    .append("<span style=\"color:#84A50A;\">").append(logTimeStamp).append("</span>")
                    .append("<span style=\"color:#d629c9;margin:0px 15px;\">").append(tag).append("</span>")
                    .append("<strong style=\"color:#555753;\">").append(getPriorityName(priority)).append("</strong>")
                when (priority) {
                    2 -> writer.append("<span style=\"color:#4367B8;margin:0px 15px;\">").append(message)
                        .append("</span>")
                    3 -> writer.append("<span style=\"color:#008c25;margin:0px 15px;\">").append(message)
                        .append("</span>")
                    4 -> writer.append("<span style=\"color:#523927;margin:0px 15px;\">").append(message)
                        .append("</span>")
                    5 -> writer.append("<span style=\"color:#C4A003;margin:0px 15px;\">").append(message)
                        .append("</span>")
                    6 -> writer.append("<strong style=\"color:#EF2929;margin:0px 15px;\">").append(message)
                        .append("</strong>")
                    else -> writer.append("<strong style=\"color:#00688B;margin:0px 15px;\">").append(message)
                        .append("</strong>")
                }
                writer.append("</p>")
                writer.flush()
                writer.close()
            }
        } catch (e: Exception) { Timber.e(e) }
    }

    private fun getPriorityName(priority: Int): String {
        return when (priority) {
            2 -> "VERBOSE"
            3 -> "DEBUG"
            4 -> "INFO"
            5 -> "WARN"
            6 -> "ERROR"
            else -> "OTHER"
        }
    }

    override fun createStackElementTag(element: StackTraceElement): String {
        return super.createStackElementTag(element) + " - " + element.lineNumber
    }

    private fun generateFile(path: String, fileName: String): File? {
        var file: File? = null
        if (isExternalStorageAvailable) {
            val root = File(
                context.getExternalFilesDir(null),
                path
            )
            var dirExists = true
            if (!root.exists()) {
                dirExists = root.mkdirs()
            }
            if (dirExists) {
                file = File(root, fileName)
            }
        }
        return file
    }

    private val isExternalStorageAvailable: Boolean
        get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
}
