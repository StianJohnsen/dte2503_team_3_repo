package com.example.dashcarr.presentation.tabs.settings.maps_settings

import android.content.Context
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

/**
 * A fragment for managing map-related settings, including points of interest.
 *
 * @property viewModel An instance of the MapsSettingsViewModel for managing fragment data and logic.
 * @property pointsAdapter A lazy-initialized adapter for displaying a list of points of interest.
 */
@AndroidEntryPoint
class MapsSettingsFragment : BaseFragment<FragmentMapsSettingsBinding>(
    FragmentMapsSettingsBinding::inflate,
    showBottomNavBar = false
), PointOfInterestRecyclerAdapter.OnPointClickListener {

    private val viewModel: MapsSettingsViewModel by viewModels()
    private val pointsAdapter by lazy {
        PointOfInterestRecyclerAdapter(this)
    }

    /**
     * Initializes the view model and pointsAdapter, sets up the RecyclerView, initializes listeners,
     * and observes the view model for updates.
     *
     * @param view The fragment's root view.
     * @param savedInstanceState The saved state of the fragment.
     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        initListeners()
        observeViewModel()
        loadCurrentTileChoice()

        binding.buttonChangeTile.setOnClickListener {
            viewModel.nextTileName()
            viewModel.currentTileNameResId.observe(viewLifecycleOwner) { tileNameResId ->
                var tileName = getString(tileNameResId)
                tileName = getTileSourceFromName(tileName)
                binding.txtTile.text = getString(R.string.current_tile, tileName)
                viewModel.saveCurrentTileName(tileNameResId)
            }
        }

    }

    /**
     * Observes various LiveData properties within the ViewModel and reacts to changes by updating
     * the UI accordingly. For example, it listens for changes in the list of points of interest,
     * visibility of dialogs, and success or failure messages related to point operations.
     */
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

    /**
     * Initializes click listeners for buttons and UI elements in the fragment, allowing actions
     * like deleting, renaming points, and navigating back.
     */
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

    /**
     * Sets up the RecyclerView and assigns the `pointsAdapter` to it, along with specifying
     * the layout manager.
     */
    private fun setupAdapter() {
        binding.recyclerView.adapter = pointsAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    /**
     * Handles the click event when deleting a point of interest and shows a confirmation dialog.
     *
     * @param point The PointOfInterestEntity to be deleted.
     */
    override fun onDelete(point: PointOfInterestEntity) {
        viewModel.showConfirmDeleteDialog(point)
    }

    /**
     * Handles the click event when renaming a point of interest and shows a rename dialog.
     *
     * @param point The PointOfInterestEntity to be updated.
     */
    override fun onUpdate(point: PointOfInterestEntity) {
        viewModel.showRenameDialog(point)
    }

    private fun loadCurrentTileChoice() {
        val sharedPrefs = requireActivity().getSharedPreferences("MapPrefs", Context.MODE_PRIVATE)
        val tileNameResId = sharedPrefs.getInt("current_tile_name_res_id", R.string.mapnik)
        var tileName = getString(tileNameResId)
        tileName = getTileSourceFromName(tileName)
        binding.txtTile.text = getString(R.string.current_tile, tileName)
    }

    private fun getTileSourceFromName(tileName: String): String {
        return when (tileName) {
            getString(R.string.mapnik) -> "Standard"
            getString(R.string.openTopo) -> "Relief"
            else -> "Standard"
        }
    }
}