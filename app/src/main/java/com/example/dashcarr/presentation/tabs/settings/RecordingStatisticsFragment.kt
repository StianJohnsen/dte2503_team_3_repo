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
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit


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

        val jsonArray = readJsonFromFile("sensor_config.json")

        val categoryCount = mutableMapOf<String, Float>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            if (jsonObject.has("unfil_gyro")) categoryCount["unfil_gyro"] =
                categoryCount.getOrDefault("unfil_gyro", 0f) + 1f
            if (jsonObject.has("unfil_accel")) categoryCount["unfil_accel"] =
                categoryCount.getOrDefault("unfil_accel", 0f) + 1f
            if (jsonObject.has("fil_gyro")) categoryCount["fil_gyro"] = categoryCount.getOrDefault("fil_gyro", 0f) + 1f
            if (jsonObject.has("fil_accel")) categoryCount["fil_accel"] =
                categoryCount.getOrDefault("fil_accel", 0f) + 1f
        }

        val entries = ArrayList<PieEntry>()

        for ((key, value) in categoryCount) {
            entries.add(PieEntry(value, key))
        }

        val pieChart: PieChart = view.findViewById(R.id.pieChart_stats)
        val dataSet = PieDataSet(entries, "Categories")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

        pieChart.setDrawEntryLabels(false)

        val data = PieData(dataSet)
        pieChart.data = data

        pieChart.description.isEnabled = false
        pieChart.legend.textColor = Color.WHITE

        pieChart.invalidate()

        var totalElapsedTime: Long = 0
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val elapsedTimeStr = jsonObject.getString("elapsed_time")
            val elapsedTimeDate = dateFormat.parse(elapsedTimeStr)
            val elapsedTimeMillis = elapsedTimeDate?.time ?: 0L

            // Convertir le temps en millisecondes vers des secondes
            val elapsedTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis)

            totalElapsedTime += elapsedTimeSeconds
        }

        val averageElapsedTime = if (jsonArray.length() > 0) totalElapsedTime / jsonArray.length() else 0

        val averageElapsedTimeStr = String.format(
            "%02d:%02d:%02d",
            TimeUnit.SECONDS.toHours(averageElapsedTime),
            TimeUnit.SECONDS.toMinutes(averageElapsedTime) % 60,
            averageElapsedTime % 60
        )

        binding.inputAvgTime.text = "$averageElapsedTimeStr"

        val averageSizeFile = averageFileSize(requireContext())

        binding.inputAvgSize.text = "$averageSizeFile"

        val dir = File(requireContext().filesDir, "")
        val size = getFolderSize(dir)
        val fileSizeInMB = size.toDouble() / (1024 * 1024)

        binding.inputTotalStorage.text = "%.2f MB".format(fileSizeInMB)

        val files = context?.fileList()
        val numberOfFiles = files?.size
        binding.inputNumberOfFiles.text = "$numberOfFiles"
    }

    fun getFolderSize(folder: File): Long {
        var length: Long = 0
        val files = folder.listFiles()

        if (files != null) {
            for (file in files) {
                if (file.isFile) {
                    length += file.length()
                } else {
                    length += getFolderSize(file)
                }
            }
        }
        return length
    }

    fun readJsonFromFile(fileName: String): JSONArray {
        var jsonArray = JSONArray()
        try {
            val inputStream = context?.openFileInput(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
            val line = reader.readLine()
            jsonArray = JSONArray(line)
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return jsonArray
    }

    fun averageFileSize(context: Context): String {
        val files = context.fileList()
        var totalSize = 0
        var fileCount = 0

        for (fileName in files) {
            val inputStream = context.openFileInput(fileName)
            val fileSize = inputStream.available()
            totalSize += fileSize
            fileCount++
            inputStream.close()
        }

        val averageSizeInKB = if (fileCount > 0) totalSize.toDouble() / fileCount / 1024 else 0.0
        return String.format("%.2f", averageSizeInKB) + " KB"
    }
}