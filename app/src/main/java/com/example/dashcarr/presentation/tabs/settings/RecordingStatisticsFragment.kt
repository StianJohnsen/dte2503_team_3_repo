package com.example.dashcarr.presentation.tabs.settings

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentRecordingStatisticsBinding
import com.example.dashcarr.presentation.core.BaseFragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.IOException
import java.io.InputStream


class RecordingStatisticsFragment : BaseFragment<FragmentRecordingStatisticsBinding>(
    FragmentRecordingStatisticsBinding::inflate,
    showBottomNavBar = true
) {
    private lateinit var bottomNavigationView: BottomNavigationView
    private val viewModel: RecordingStatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun observeViewModel() {
        TODO("Not yet implemented")
    }

    override fun initListeners() {
        TODO("Not yet implemented")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            recordingStatisticsFragment = this@RecordingStatisticsFragment
        }
        binding.imageBackStats.setOnClickListener {
            findNavController().navigate(R.id.action_action_statistics_to_SavedRecordingsFragment)
        }

        val pieChart: PieChart = view.findViewById(R.id.pieChart_stats)

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(40f, "Accident recordings"))
        entries.add(PieEntry(30f, "Recordings"))
        entries.add(PieEntry(20f, "Category 3"))
        entries.add(PieEntry(10f, "Category 4"))

        val dataSet = PieDataSet(entries, "Categories")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        pieChart.setDrawEntryLabels(false)

        val data = PieData(dataSet)
        pieChart.data = data

        pieChart.description.isEnabled = false
        pieChart.legend.textColor = Color.WHITE

        pieChart.invalidate()

        val totalSizeInMB = getTotalAssetsSize(requireContext())

        binding.inputTotalStorage.text = "%.2f MB".format(totalSizeInMB)

        val numberOfFiles = getNumberOfFilesInAssets(requireContext())
        binding.inputNumberOfFiles.text = "$numberOfFiles"
    }

    fun getTotalAssetsSize(context: Context): Double {
        var totalSize = 0.0
        try {
            val assetManager = context.assets
            val files = assetManager.list("")
            if (files != null) {
                for (fileName in files) {
                    var inputStream: InputStream? = null
                    try {
                        inputStream = assetManager.open(fileName)
                        val size = inputStream.available().toDouble()
                        totalSize += size
                    } catch (e: Exception) {
                    } finally {
                        inputStream?.close()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return totalSize / (1024 * 1024)
    }

    fun getNumberOfFilesInAssets(context: Context): Int {
        var numberOfFiles = 0
        try {
            val assetManager = context.assets
            val files = assetManager.list("")
            if (files != null) {
                for (fileName in files) {
                    try {
                        context.assets.open(fileName).use { inputStream ->
                            numberOfFiles++
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return numberOfFiles
    }
}