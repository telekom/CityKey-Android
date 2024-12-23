package com.telekom.citykey.view.dialogs.dpn_updates

import android.os.Bundle
import android.view.View
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DataPrivacyUpdatesBinding
import com.telekom.citykey.domain.legal_data.LegalDataManager
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.decodeHTML
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import com.telekom.citykey.view.dialogs.DataPrivacyNoticeDialog
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DpnUpdatesDialog(private val acceptListener: () -> Unit) :
    FullScreenBottomSheetDialogFragment(R.layout.data_privacy_updates) {

    private val legalData: LegalDataManager by inject()
    private val viewModel: DpnUpdatesViewModel by viewModel()
    private val binding: DataPrivacyUpdatesBinding by viewBinding(DataPrivacyUpdatesBinding::bind)

    init {
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnPrivacy.setupOutlineStyle()

        binding.btnOk.setOnClickListener {
            binding.btnOk.startLoading()
            viewModel.onChangesAccepted()
        }

        binding.btnPrivacy.setOnClickListener {
            DataPrivacyNoticeDialog().showDialog(this.parentFragmentManager, "DataPrivacyNoticeDialog")
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.btnOk.stopLoading()
            DialogUtil.showTechnicalError(requireContext())
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired) {
                viewModel.onRetryCanceled()
                binding.btnOk.stopLoading()
            }
        }

        viewModel.newDpnAccepted.observe(viewLifecycleOwner) {
            acceptListener()
            dismiss()
        }

        legalData.legalInfo.observe(viewLifecycleOwner) {
            binding.description.text = it.dataSecurity.noticeText.decodeHTML()
        }
    }
}
