package com.example.dashcarr.presentation.tabs.settings

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentRecordingDetailsBinding
import com.example.dashcarr.presentation.core.BaseFragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset


class RecordingDetailsFragment : BaseFragment<FragmentRecordingDetailsBinding>(
    FragmentRecordingDetailsBinding::inflate,
    showBottomNavBar = true
) {
    private lateinit var bottomNavigationView: BottomNavigationView
    private val viewModel: RecordingDetailsViewModel by viewModels()

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

        val args: RecordingDetailsFragmentArgs by navArgs()
        val fileName = args.selectedFileName

        binding.textRecordingName.text = fileName

        try {
            println("FILENAME: $fileName")
            val inputStream: InputStream = requireContext().assets.open("$fileName.csv")
            val fileSize = inputStream.available()
            val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
            var line: String? = ""
            var lastID: String? = ""
            val entriesAccelX = ArrayList<Entry>()
            val entriesAccelY = ArrayList<Entry>()
            val entriesAccelZ = ArrayList<Entry>()
            val entriesGyroX = ArrayList<Entry>()
            val entriesGyroY = ArrayList<Entry>()
            val entriesGyroZ = ArrayList<Entry>()

            reader.readLine()
            while (reader.readLine().also { line = it } != null) {
                val tokens = line?.split(",") ?: continue
                lastID = tokens[0]
            }
            reader.close()
            inputStream.close()

            val inputStream2: InputStream = requireContext().assets.open("$fileName.csv")
            val reader2 = BufferedReader(InputStreamReader(inputStream2, Charset.forName("UTF-8")))
            reader2.readLine()
            var lineNumber = 0
            while (reader2.readLine().also { line = it } != null) {
                val tokens = line?.split(",") ?: continue
                if (tokens.size < 8) continue
                entriesAccelX.add(Entry(lineNumber.toFloat(), tokens[2].toFloat()))
                entriesAccelY.add(Entry(lineNumber.toFloat(), tokens[3].toFloat()))
                entriesAccelZ.add(Entry(lineNumber.toFloat(), tokens[4].toFloat()))
                entriesGyroX.add(Entry(lineNumber.toFloat(), tokens[6].toFloat()))
                entriesGyroY.add(Entry(lineNumber.toFloat(), tokens[7].toFloat()))
                entriesGyroZ.add(Entry(lineNumber.toFloat(), tokens[8].toFloat()))
                lineNumber++
            }

            reader2.close()
            inputStream2.close()

            val fileSizeInMB = fileSize.toDouble() / (1024 * 1024)
            binding.inputLength.text = "%.2f MB".format(fileSizeInMB)
            binding.inputAmountOfDatapoints.text = "$lastID"

            val lineDataSetAccelX = LineDataSet(entriesAccelX, "Accel_X")
            lineDataSetAccelX.setColor(Color.RED)
            lineDataSetAccelX.setDrawCircles(false)

            val lineDataSetAccelY = LineDataSet(entriesAccelY, "Accel_Y")
            lineDataSetAccelY.setColor(Color.GREEN)
            lineDataSetAccelY.setDrawCircles(false)

            val lineDataSetAccelZ = LineDataSet(entriesAccelZ, "Accel_Z")
            lineDataSetAccelZ.setColor(Color.BLUE)
            lineDataSetAccelZ.setDrawCircles(false)

            val lineDataSetGyroX = LineDataSet(entriesGyroX, "Gyro_X")
            lineDataSetGyroX.setColor(Color.YELLOW)
            lineDataSetGyroX.setDrawCircles(false)

            val lineDataSetGyroY = LineDataSet(entriesGyroY, "Gyro_Y")
            lineDataSetGyroY.setColor(Color.CYAN)
            lineDataSetGyroY.setDrawCircles(false)

            val lineDataSetGyroZ = LineDataSet(entriesGyroZ, "Gyro_Z")
            lineDataSetGyroY.setColor(Color.MAGENTA)
            lineDataSetGyroZ.setDrawCircles(false)

            val dataSetsAccel: ArrayList<ILineDataSet> = ArrayList()
            dataSetsAccel.add(lineDataSetAccelX)
            dataSetsAccel.add(lineDataSetAccelY)
            dataSetsAccel.add(lineDataSetAccelZ)

            val dataSetsGyro: ArrayList<ILineDataSet> = ArrayList()
            dataSetsGyro.add(lineDataSetGyroX)
            dataSetsGyro.add(lineDataSetGyroY)
            dataSetsGyro.add(lineDataSetGyroZ)

            val data = LineData(dataSetsAccel)
            val lineChart: LineChart = view.findViewById(R.id.graph_accel)
            lineChart.data = data
            val legend = lineChart.legend
            legend.textColor = Color.WHITE
            legend.isEnabled = true

            val xAxis = lineChart.xAxis
            xAxis.textColor = Color.WHITE

            val yAxisLeft = lineChart.axisLeft
            yAxisLeft.textColor = Color.WHITE

            val yAxisRight = lineChart.axisRight
            yAxisRight.textColor = Color.WHITE
            lineChart.invalidate()

            val data1 = LineData(dataSetsGyro)
            val lineChart1: LineChart = view.findViewById(R.id.graph_gyro)
            lineChart1.data = data1
            val legend1 = lineChart1.legend
            legend1.textColor = Color.WHITE
            legend1.isEnabled = true
            val xAxis1 = lineChart1.xAxis
            xAxis1.textColor = Color.WHITE

            val yAxisLeft1 = lineChart1.axisLeft
            yAxisLeft1.textColor = Color.WHITE

            val yAxisRight1 = lineChart1.axisRight
            yAxisRight1.textColor = Color.WHITE
            lineChart1.invalidate()

        } catch (e: IOException) {
            e.printStackTrace()
        }

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            recordingDetailsFragment = this@RecordingDetailsFragment
        }
        binding.imageBackDetails.setOnClickListener {
            findNavController().navigate(R.id.action_action_details_to_SavedRecordingsFragment)
        }
    }
}