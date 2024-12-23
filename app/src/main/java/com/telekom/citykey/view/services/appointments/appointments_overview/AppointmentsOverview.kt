package com.telekom.citykey.view.services.appointments.appointments_overview

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.core.widget.TextViewCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.telekom.citykey.R
import com.telekom.citykey.databinding.AppointmentsOverviewFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.services.appointments.AppointmentsState
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.*
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppointmentsOverview : MainFragment(R.layout.appointments_overview_fragment) {

    private val viewModel: AppointmentsOverviewViewModel by viewModel()
    private val binding by viewBinding(AppointmentsOverviewFragmentBinding::bind)
    private var adapter: AppointmentsAdapter? = null
    private var swipeCallbacks: AppointmentsSwipeCallbacks? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribeUi()
    }

    private fun initViews() {
        setupToolbar(binding.toolbarAppointments)
        swipeCallbacks?.actionColor = CityInteractor.cityColorInt

        adapter = AppointmentsAdapter(
            deleteListener = viewModel::onDelete,
            itemSwipeListener = ::swipeNotAllowed
        )

        val services = context?.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (!services.isEnabled) {
            adapter.also { appointmentsAdapter ->
                swipeCallbacks = appointmentsAdapter?.let {
                    AppointmentsSwipeCallbacks(
                        it,
                        requireContext()
                    )
                }.also {
                    it?.let { itemTouch -> ItemTouchHelper(itemTouch).attachToRecyclerView(binding.appointmentsList) }
                }
            }
        }
        binding.appointmentsList.adapter = adapter
        binding.retryButton.setTextColor(CityInteractor.cityColorInt)
        TextViewCompat.setCompoundDrawableTintList(
            binding.retryButton,
            ColorStateList.valueOf(CityInteractor.cityColorInt)
        )
        binding.swipeRefreshLayout.setOnRefreshListener(viewModel::onRefresh)
        binding.retryButton.setOnClickListener {
            binding.loading.setVisible(true)
            binding.errorLayout.setVisible(false)
            viewModel.onRefresh()
        }
    }

    private fun swipeNotAllowed(position: Int) {
        adapter?.notifyItemChanged(position)
        showInfoSnackBar(R.string.appnmt_delete_not_allowed)
    }

    private fun subscribeUi() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("DeletionSuccessful")
            ?.observe(viewLifecycleOwner) { result ->
                if (result) {
                    showUndoDeletionSnackBar()
                    findNavController().currentBackStackEntry?.savedStateHandle?.set("DeletionSuccessful", false)
                }
            }
        viewModel.appointmentsData.observe(viewLifecycleOwner) { appointments ->
            adapter?.updateData(appointments)
        }

        viewModel.state.observe(viewLifecycleOwner) {
            binding.loading.setVisible(false)
            binding.swipeRefreshLayout.isRefreshing = false
            if (it == AppointmentsState.UserNotLoggedIn) {
                findNavController().popBackStack(R.id.services, false)
                //TODO: Refer todo of `AppointmentsInteractor` and remove this when not needed
                with(requireActivity() as MainActivity) {
                    hideSplashScreen()
                    clearDeeplinkInfo()
                }
            } else {
                binding.appointmentsList.setVisible(it == AppointmentsState.Success)
                binding.errorLayout.setVisible(it == AppointmentsState.Error)
                binding.textEmptyState.setVisible(it == AppointmentsState.Empty)
            }
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(
                context = requireContext(),
                onRetry = viewModel::onRetryRequired,
                onCancel = {
                    binding.swipeRefreshLayout.isRefreshing = false
                    viewModel.onRetryCanceled()
                }
            )
        }

        viewModel.userLoggedOut.observe(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.services, false)
        }

        viewModel.stopRefreshSpinner.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.serviceIsAvailable.observe(viewLifecycleOwner) { serviceIsAvailable ->
            if (serviceIsAvailable.not()) {
                findNavController().navigate(
                    R.id.services,
                    args = null,
                    NavOptions.Builder().setPopUpTo(R.id.services_graph, false).build()
                )
            }
            with(requireActivity() as MainActivity) {
                hideSplashScreen()
                clearDeeplinkInfo()
            }
        }
        viewModel.deletionSuccessful.observe(viewLifecycleOwner) {
            showUndoDeletionSnackBar()
        }
        viewModel.showNoInternetDialog.observe(viewLifecycleOwner) {
            DialogUtil.showNoInternetDialog(requireContext())
        }
        viewModel.showErrorSnackbar.observe(viewLifecycleOwner) {
            showInfoSnackBar(R.string.appnmt_delete_error_snackbar)
        }
    }

    private fun showUndoDeletionSnackBar() {
        showActionSnackbar(
            messageId = R.string.apnmt_snackbar_undo,
            actionTextId = R.string.b_002_infobox_snackbar_action_undo
        ) { viewModel.onUndoDeletion() }
    }
}
