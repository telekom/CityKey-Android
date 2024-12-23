package com.telekom.citykey.view.services.defect_reporter.category_selection.defectsubcategory

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DefectSubcategorySelectionFragmentBinding
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.services.defect_reporter.location_selection.DefectLocationSelection

class DefectSubcategorySelection : MainFragment(R.layout.defect_subcategory_selection_fragment) {
    private val binding by viewBinding(DefectSubcategorySelectionFragmentBinding::bind)
    private val args: DefectSubcategorySelectionArgs by navArgs()
    private var listAdapter: DefectSubcategorySelectionAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.defectSubcategoryToolbar)
        binding.defectSubcategoryToolbar.title = args.defectCategory.serviceName
        setUpAdapter()
    }

    private fun setUpAdapter() {
        listAdapter =
            DefectSubcategorySelectionAdapter(
                categoryResultListener = { defectSubcategory ->
                    DefectLocationSelection(
                        locationResultListener = {
                            findNavController().navigate(
                                DefectSubcategorySelectionDirections.toDefectReportForm2(
                                    args.service,
                                    it,
                                    args.defectCategory.serviceName,
                                    args.defectCategory.serviceCode,
                                    defectSubcategory.serviceName,
                                    defectSubcategory.serviceCode,
                                    defectSubcategory.description,
                                    defectSubcategory.hasAdditionalInfo ?: false
                                )
                            )
                        }
                    ).showDialog(
                        childFragmentManager
                    )
                }
            )
        binding.subategoryList.adapter = listAdapter
        listAdapter?.submitList(args.defectCategory.subCategories)
    }

    override fun onDestroy() {
        super.onDestroy()
        listAdapter = null
    }
}
