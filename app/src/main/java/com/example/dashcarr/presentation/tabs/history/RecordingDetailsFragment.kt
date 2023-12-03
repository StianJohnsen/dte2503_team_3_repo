package com.example.dashcarr.presentation.tabs.history

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
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Fragment for displaying detailed information and graphical representations of a specific recording.
 * Provides functionalities to view line charts or bar charts based on the sensor data,
 * and displays relevant details about the recording.
 *
 * Manages the layout and interaction for showing recording details such as elapsed time, file size,
 * and sensor data visualizations. Supports both line and bar charts to represent different types of data.
 */
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
        val elapsedTime = args.elapsedTime
        binding.textRecordingName.text = args.title

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
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        binding.inputFileDate.text = LocalDateTime.parse(args.date).format(formatter)
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
        lineDataSetX.color = Color.BLUE
        lineDataSetX.setDrawCircles(false)

        val lineDataSetY = LineDataSet(entriesY, "Y")
        lineDataSetY.color = Color.GREEN
        lineDataSetY.setDrawCircles(false)

        val lineDataSetZ = LineDataSet(entriesZ, "Z")
        lineDataSetZ.color = Color.YELLOW
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
        legend.textSize = 16F
        legend.formSize = 16F

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
                segmentColors.add(HistoryFragment.CarState.valueOf(lastValue).color)
                range = 1
                lastValue = value
            }
        }
        segmentSizes.add(range.toFloat())
        segmentColors.add(HistoryFragment.CarState.valueOf(lastValue).color)
        val entry = BarEntry(0F, segmentSizes.toFloatArray())
        val dataset = BarDataSet(listOf(entry), "Car states")
        dataset.setDrawValues(false)
        dataset.colors = segmentColors

        val barData = BarData(dataset)
        barData.isHighlightEnabled = false
        val barChart = HorizontalBarChart(context)
        barChart.data = barData
        barChart.setFitBars(true)

        barChart.xAxis.textColor = Color.TRANSPARENT
        barChart.axisRight.textColor = Color.TRANSPARENT
        barChart.axisLeft.textColor = Color.WHITE
        barChart.isScaleYEnabled = false

        val legendEntries =
            HistoryFragment.CarState.entries.filterNot { it == HistoryFragment.CarState.UNKNOWN }.map {
                LegendEntry(
                    it.name[0] + it.name.lowercase().substring(1),
                    barChart.legend.form,
                    16F,
                    Float.NaN,
                    null,
                    it.color
                )
            }
        barChart.legend.textColor = Color.WHITE
        barChart.legend.textSize = 16F
        barChart.legend.isWordWrapEnabled = true
        barChart.legend.setCustom(legendEntries)

        barChart.description.isEnabled = false
        (view?.findViewById(R.id.graph_gyro) as FrameLayout).addView(barChart)
        barChart.invalidate()
    }

}