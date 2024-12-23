package com.telekom.citykey.view.services.defect_reporter.category_selection

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DefectCategorySelectionFragmentBinding
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.services.defect_reporter.location_selection.DefectLocationSelection
import org.koin.androidx.viewmodel.ext.android.viewModel

class DefectCategorySelection : MainFragment(R.layout.defect_category_selection_fragment) {
    private val viewModel: DefectCategorySelectionViewModel by viewModel()
    private val binding by viewBinding(DefectCategorySelectionFragmentBinding::bind)
    private var listAdapter: DefectCategorySelectionAdapter? = null
    private val args: DefectCategorySelectionArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.defectCategoryToolbar)
        setUpAdapter()
        subscribeUi()
    }

    fun subscribeUi() {
        viewModel.defectReporterCategories.observe(viewLifecycleOwner) {
            listAdapter?.submitList(it)
        }
    }

    private fun setUpAdapter() {
        listAdapter = DefectCategorySelectionAdapter(
            categoryResultListener = { defectCategory ->
                if (defectCategory.subCategories.isNullOrEmpty()) {
                    DefectLocationSelection(
                        locationResultListener = {
                            findNavController().navigate(
                                DefectCategorySelectionDirections.toDefectReportForm2(
                                    args.service,
                                    it,
                                    defectCategory.serviceName,
                                    defectCategory.serviceCode,
                                    null,
                                    null,
                                    null,
                                    false
                                )
                            )
                        }
                    ).showDialog(
                        childFragmentManager
                    )
                } else {
                    findNavController().navigate(
                        DefectCategorySelectionDirections.actionDefectCategorySelectionToDefectSubcategorySelection(
                            args.service,
                            defectCategory
                        )
                    )
                }
            }
        )
        binding.categoryList.adapter = listAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        listAdapter = null
    }
}
