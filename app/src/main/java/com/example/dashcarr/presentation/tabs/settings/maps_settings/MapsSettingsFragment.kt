package com.example.dashcarr.presentation.tabs.settings.maps_settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentMapsSettingsBinding
import com.example.dashcarr.domain.entity.PointOfInterestEntity
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.extensions.hideKeyboard
import com.example.dashcarr.extensions.toastErrorUnknownShort
import com.example.dashcarr.extensions.toastShort
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.tabs.settings.maps_settings.adapter.PointOfInterestRecyclerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapsSettingsFragment : BaseFragment<FragmentMapsSettingsBinding>(
    FragmentMapsSettingsBinding::inflate,
    showBottomNavBar = false ), PointOfInterestRecyclerAdapter.OnPointClickListener
{

    private val viewModel: MapsSettingsViewModel by viewModels()
    private val pointsAdapter by lazy {
        PointOfInterestRecyclerAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        initListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.pointsOfInterest.observe(viewLifecycleOwner) {
            pointsAdapter.submitList(it)
        }
        viewModel.showDeleteDialog.collectWithLifecycle(viewLifecycleOwner) {
            if (!it) hideKeyboard()
            binding.flDeleteMarkerLayout.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.showRenameDialog.collectWithLifecycle(viewLifecycleOwner) {
            if (!it) hideKeyboard()
            binding.flEditMarkerLayout.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.onDeletePointSuccess.collectWithLifecycle(viewLifecycleOwner) {
            toastShort(getString(R.string.point_deleted_successfully))
        }
        viewModel.onDeletePointFailure.collectWithLifecycle(viewLifecycleOwner) {
            toastErrorUnknownShort()
        }
        viewModel.onRenamePointSuccess.collectWithLifecycle(viewLifecycleOwner) {
            toastShort(getString(R.string.point_renamed_successfully))
        }
        viewModel.onRenamePointFailure.collectWithLifecycle(viewLifecycleOwner) {
            toastErrorUnknownShort()
        }
    }

    private fun initListeners() {
        binding.btnDelete.setOnClickListener {
            viewModel.deletePoint()
        }
        binding.btnCancelDelete.setOnClickListener {
            viewModel.hideDeleteDialog()
        }
        binding.btnUpdate.setOnClickListener {
            viewModel.updatePoint(binding.etMarkerName.text.toString())
        }
        binding.btnCancel.setOnClickListener {
            viewModel.hideRenameDialog()
        }
        binding.btnBackToSettings.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupAdapter() {
        binding.recyclerView.adapter = pointsAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDelete(point: PointOfInterestEntity) {
        viewModel.showConfirmDeleteDialog(point)
    }

    override fun onUpdate(point: PointOfInterestEntity) {
        viewModel.showRenameDialog(point)
    }


}