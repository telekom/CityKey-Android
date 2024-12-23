package com.telekom.citykey.view.services.waste_calendar.export.options

import android.os.Bundle
import android.view.View
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WasteExportOptionsBinding
import com.telekom.citykey.models.waste_calendar.CalendarAccount
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment

class WasteExportSelectionDialog(
    private val calendarAcc: List<CalendarAccount>,
    private val selectedCalendar: CalendarAccount,
    val resultListener: (CalendarAccount) -> Unit
) : FullScreenBottomSheetDialogFragment(R.layout.waste_export_options) {
    private val binding by viewBinding(WasteExportOptionsBinding::bind)
    private var listAdapter: WasteExportSelectionAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        setAccessibilityRoleForToolbarTitle(binding.toolbar)

        listAdapter = WasteExportSelectionAdapter(selectedCalendar) {
            resultListener(it)
            dismiss()
        }

        binding.optionList.adapter = listAdapter
        listAdapter?.submitList(calendarAcc)
    }

    override fun dismiss() {
        listAdapter = null
        super.dismiss()
    }
}
