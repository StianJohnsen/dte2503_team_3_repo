package com.example.dashcarr.presentation.tabs.history

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentRecordingStatisticsBinding
import com.example.dashcarr.presentation.core.BaseFragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * Fragment for displaying recording statistics.
 */
class RecordingStatisticsFragment : BaseFragment<FragmentRecordingStatisticsBinding>(
    FragmentRecordingStatisticsBinding::inflate,
    showBottomNavBar = false
) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }


    /**
     * Initializes UI and loads data once the view is created.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            recordingStatisticsFragment = this@RecordingStatisticsFragment
        }
        binding.imageBackStats.setOnClickListener {
            findNavController().popBackStack()
            //findNavController().navigate(R.id.action_action_statistics_to_SavedRecordingsFragment)
        }

        val jsonArray = readJsonFromFile()

        val categoryCount = mutableMapOf<String, Float>()

        // Process JSON data for pie chart
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            if (jsonObject.has("unfil_gyro")) {
                categoryCount["unfil_gyro"] = categoryCount.getOrDefault("unfil_gyro", 0f) + 1f
            }

            if (jsonObject.has("unfil_accel")) {
                categoryCount["unfil_accel"] = categoryCount.getOrDefault("unfil_accel", 0f) + 1f
            }

            if (jsonObject.has("fil_gyro")) {
                categoryCount["fil_gyro"] = categoryCount.getOrDefault("fil_gyro", 0f) + 1f
            }

            if (jsonObject.has("fil_accel")) {
                categoryCount["fil_accel"] = categoryCount.getOrDefault("fil_accel", 0f) + 1f
            }
        }

        val entries = ArrayList<PieEntry>()

        for ((key, value) in categoryCount) {
            entries.add(PieEntry(value, key))
        }

        // Customize the pie Chart
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

        // Calculate the average elapsed time of records
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val elapsedTimeStr = jsonObject.getString("elapsed_time")

            val timeParts = elapsedTimeStr.split(":")
            val hours = timeParts[0].toLong()
            val minutes = timeParts[1].toLong()
            val seconds = timeParts[2].toLong()

            val elapsedTimeSeconds = hours * 3600 + minutes * 60 + seconds

            totalElapsedTime += elapsedTimeSeconds
        }

        val averageElapsedTime = if (jsonArray.length() > 0) totalElapsedTime / jsonArray.length() else 0

        val averageElapsedTimeStr = String.format(
            "%02d:%02d:%02d",
            TimeUnit.SECONDS.toHours(averageElapsedTime),
            TimeUnit.SECONDS.toMinutes(averageElapsedTime) % 60,
            averageElapsedTime % 60
        )

        binding.inputAvgTime.text = averageElapsedTimeStr

        val averageSizeFile = averageFileSize(requireContext())

        binding.inputAvgSize.text = averageSizeFile

        val dir = File(requireContext().filesDir, "")
        val size = getFolderSize(dir)
        val fileSizeInMB = size / (1024 * 1024)
        val totalStorage = "%.2f MB".format(fileSizeInMB)

        binding.inputTotalStorage.text = totalStorage

        val files = context?.fileList()?.filter { it.endsWith(".csv") }
        val numberOfCsvFiles = files?.size

        binding.inputNumberOfFiles.text = "$numberOfCsvFiles"
    }

    /**
     * Returns the total size of a folder.
     */
    private fun getFolderSize(folder: File): Double {
        var length = 0.0
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

    /**
     * Reads a JSONArray from a given file.
     */
    private fun readJsonFromFile(): JSONArray {
        var jsonArray = JSONArray()
        val fileName = "sensor_config.json"
        try {
            val file = File(requireContext().filesDir, fileName)
            if (file.exists() && file.isFile) {
                val inputStream = context?.openFileInput(fileName)
                val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
                val line = reader.readLine()
                jsonArray = JSONArray(line)
                inputStream?.close()
            } else {
                println("File Not Found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return jsonArray
    }

    /**
     * Calculates the average file size in the files folder
     */
    private fun averageFileSize(context: Context): String {
        val files = context.fileList()
        var totalSize = 0
        var fileCount = 0

        for (fileName in files) {
            val file = File(requireContext().filesDir, fileName)
            if (file.exists() && file.isFile) {
                val inputStream = context.openFileInput(fileName)
                val fileSize = inputStream.available()
                totalSize += fileSize
                fileCount++
                inputStream.close()
            } else {
                println("File Not Found")
            }
        }

        val averageSizeInKB = if (fileCount > 0) totalSize.toDouble() / fileCount / 1024 else 0.0
        return String.format("%.2f", averageSizeInKB) + " KB"
    }
}