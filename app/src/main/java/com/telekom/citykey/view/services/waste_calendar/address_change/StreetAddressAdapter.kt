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

package com.telekom.citykey.view.services.waste_calendar.address_change

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class StreetAddressAdapter(context: Context, val resource: Int) : ArrayAdapter<String>(context, resource) {
    private val addressSuggestionList = mutableListOf<String>()

    fun updateSuggestions(list: List<String>) {
        addressSuggestionList.clear()
        addressSuggestionList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getCount() = addressSuggestionList.size

    override fun getItem(position: Int) = addressSuggestionList[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: TextView = (convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)) as TextView
        view.text = addressSuggestionList[position]
        return view
    }
}
