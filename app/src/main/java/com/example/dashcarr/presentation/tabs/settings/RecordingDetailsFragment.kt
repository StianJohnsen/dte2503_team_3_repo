package com.example.dashcarr.presentation.tabs.settings

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentRecordingDetailsBinding
import com.example.dashcarr.presentation.core.BaseFragment
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import org.json.JSONArray
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset


class RecordingDetailsFragment : BaseFragment<FragmentRecordingDetailsBinding>(
    FragmentRecordingDetailsBinding::inflate,
    showBottomNavBar = false
) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: RecordingDetailsFragmentArgs by navArgs()
        val fileName = args.selectedFileName
        val chartType = args.chartType
        val completeFileDate = fileName.substringBefore("_")
        val elapsedTime = getElapsedTime("sensor_config.json", completeFileDate)
        val fileDate = fileName.substringBefore("T")
        val recordingName = fileDate + "\n" + fileName.substringAfter("_").substringBefore('.')
        binding.textRecordingName.text = recordingName

        val inputStream: InputStream = requireContext().openFileInput(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
        var fileLength = inputStream.available()
        val getToKB = 1024
        fileLength /= getToKB
        var line: String?

        val entries =
            Array(reader.readLine().split(",").size) { emptyList<String>().toMutableList() }

        var lineNumber = 0
        while (reader.readLine().also { line = it } != null) {
            val tokens = line?.split(",") ?: continue
            for (i in entries.indices) {
                entries[i].add(tokens[i])
            }
            lineNumber++
        }
        val amountDataPoints = lineNumber - 1
        reader.close()
        inputStream.close()

        if (chartType == "line") {
            initLineChart(
                entries[2].map { it.toFloat() },
                entries[3].map { it.toFloat() },
                entries[4].map { it.toFloat() })
        } else {
            initBarChart(entries[2])
        }

        binding.inputElapsedTime.text = elapsedTime
        binding.inputFileDate.text = fileDate
        binding.inputAmountOfDatapoints.text = "$amountDataPoints"
        val lengthInKB = "$fileLength KB"
        binding.inputDataSize.text = lengthInKB

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            recordingDetailsFragment = this@RecordingDetailsFragment
        }
        binding.imageBackDetails.setOnClickListener {
            findNavController().navigate(R.id.action_action_details_to_SavedRecordingsFragment)
        }

    }

    private fun initLineChart(valuesX: List<Float>, valuesY: List<Float>, valuesZ: List<Float>) {
        val entriesX = valuesX.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        val entriesY = valuesY.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        val entriesZ = valuesZ.mapIndexed { index, value -> Entry(index.toFloat(), value) }

        val lineDataSetX = LineDataSet(entriesX, "X")
        lineDataSetX.setColor(Color.BLUE)
        lineDataSetX.setDrawCircles(false)

        val lineDataSetY = LineDataSet(entriesY, "Y")
        lineDataSetY.setColor(Color.GREEN)
        lineDataSetY.setDrawCircles(false)

        val lineDataSetZ = LineDataSet(entriesZ, "Z")
        lineDataSetZ.setColor(Color.YELLOW)
        lineDataSetZ.setDrawCircles(false)

        val dataSetsGyro: ArrayList<ILineDataSet> = ArrayList()
        dataSetsGyro.add(lineDataSetX)
        dataSetsGyro.add(lineDataSetY)
        dataSetsGyro.add(lineDataSetZ)

        val data = LineData(dataSetsGyro)
        val lineChart = LineChart(context)
        (view?.findViewById(R.id.graph_gyro) as FrameLayout).addView(lineChart)

        val legend = lineChart.legend
        legend.textColor = Color.WHITE
        legend.isEnabled = true
        legend.textSize = 18F

        val xAxis = lineChart.xAxis
        xAxis.textColor = Color.WHITE

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.textColor = Color.WHITE

        val yAxisRight = lineChart.axisRight
        yAxisRight.textColor = Color.WHITE

        lineChart.description.isEnabled = false

        lineChart.data = data
        lineChart.invalidate()
    }

    private fun initBarChart(data: List<String>) {
        var lastValue = data[0]
        var range = 1
        val segmentSizes = emptyList<Float>().toMutableList()
        val segmentColors = emptyList<Int>().toMutableList()
        for (value in data) {
            if (value == lastValue) {
                range++
            } else {
                segmentSizes.add(range.toFloat())
                segmentColors.add(SavedRecordingsFragment.CarState.valueOf(lastValue).color)
                range = 1
                lastValue = value
            }
        }
        val entry = BarEntry(0F, segmentSizes.toFloatArray())
        val dataset = BarDataSet(listOf(entry), "Car states")
        dataset.colors = segmentColors

        val barData = BarData(dataset)
        val barChart = HorizontalBarChart(context)
        barChart.data = barData
        barChart.setFitBars(true)


        barChart.xAxis.textColor = Color.TRANSPARENT
        barChart.axisRight.textColor = Color.TRANSPARENT
        barChart.axisLeft.textColor = Color.WHITE

        val legendEntries =
            SavedRecordingsFragment.CarState.entries.filterNot { it == SavedRecordingsFragment.CarState.UNKNOWN }.map {
                LegendEntry(
                    it.name[0] + it.name.lowercase().substring(1),
                    barChart.legend.form,
                    18F,
                    Float.NaN,
                    null,
                    it.color
                )
            }
        barChart.legend.textColor = Color.WHITE
        barChart.legend.setCustom(legendEntries)

        barChart.description.isEnabled = false
        (view?.findViewById(R.id.graph_gyro) as FrameLayout).addView(barChart)
        barChart.invalidate()
    }

    fun getElapsedTime(fileName: String, csvFile: String): String {
        val jsonArray: JSONArray
        var value = ""
        try {
            val inputStream = context?.openFileInput(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
            val line = reader.readLine()
            jsonArray = JSONArray(line)
            inputStream?.close()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val name = jsonObject.getString("name")
                if (name == csvFile) {
                    value = jsonObject.getString("elapsed_time")
                    break
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return value
    }
}