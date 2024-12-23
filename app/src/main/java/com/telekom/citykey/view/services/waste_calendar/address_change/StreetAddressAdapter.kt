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
