package com.example.dashcarr.presentation.tabs.history

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

/**
 * Fragment for displaying recording details.
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

    fun observeViewModel() {
        TODO("Not yet implemented")
    }

    fun initListeners() {
        TODO("Not yet implemented")
    }

    /**
     * Initializes view elements, plot graphs, and sets other display info.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: RecordingDetailsFragmentArgs by navArgs()
        val fileName = args.selectedFileName

        // Extract time and date details
        val completeFileDate = fileName.substringBefore("_")
        val elapsedTime = getElapsedTime("sensor_config.json", completeFileDate)
        val fileDate = fileName.substringBefore("T")
        val recordingName = fileDate + "\n" + fileName.substringAfter("_").substringBefore('.')
        binding.textRecordingName.text = recordingName

        try {
            // Open file and prepare for reading
            val file = File(requireContext().filesDir, fileName)
            if (file.exists() && file.isFile) {
                val inputStream: InputStream = requireContext().openFileInput(fileName)
                val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
                var fileLength = inputStream.available()
                val getToKB = 1024
                fileLength /= getToKB
                var line: String?

                // Initialize lists for storing chart data
                val entriesX = ArrayList<Entry>()
                val entriesY = ArrayList<Entry>()
                val entriesZ = ArrayList<Entry>()

                reader.readLine()
                var lineNumber = 0

                // Read each line and populate chart data lists
                while (reader.readLine().also { line = it } != null) {
                    val tokens = line?.split(",") ?: continue
                    if (tokens.size < 5) continue
                    entriesX.add(Entry(lineNumber.toFloat(), tokens[2].toFloat()))
                    entriesY.add(Entry(lineNumber.toFloat(), tokens[3].toFloat()))
                    entriesZ.add(Entry(lineNumber.toFloat(), tokens[4].toFloat()))
                    lineNumber++
                }
                val amountDataPoints = lineNumber - 1
                reader.close()
                inputStream.close()

                // Prepare chart datasets
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

                // Customize the chart
                val data = LineData(dataSetsGyro)
                val lineChart: LineChart = view.findViewById(R.id.graph_gyro)
                val legend = lineChart.legend
                legend.textColor = Color.WHITE
                legend.isEnabled = true

                val xAxis = lineChart.xAxis
                xAxis.textColor = Color.WHITE

                val yAxisLeft = lineChart.axisLeft
                yAxisLeft.textColor = Color.WHITE

                val yAxisRight = lineChart.axisRight
                yAxisRight.textColor = Color.WHITE

                lineChart.description.isEnabled = false

                lineChart.data = data
                lineChart.invalidate()

                // Fill in the information fields
                binding.inputElapsedTime.text = elapsedTime
                binding.inputFileDate.text = fileDate
                binding.inputAmountOfDatapoints.text = "$amountDataPoints"
                val lengthInKB = "$fileLength KB"
                binding.inputDataSize.text = lengthInKB
            } else {
                println("File not found")
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            recordingDetailsFragment = this@RecordingDetailsFragment
        }

        // Navigate to History when the button is clicked
        binding.imageBackDetails.setOnClickListener {
            findNavController().navigate(R.id.action_action_details_to_SavedRecordingsFragment)
        }
    }


    /**
     * Gets elapsed time for a specific CSV file.
     * @param fileName The name of the JSON file containing metadata.
     * @param csvFile The name of the CSV file to look for.
     * @return Elapsed time as a String.
     */
    fun getElapsedTime(fileName: String, csvFile: String): String {
        val jsonArray: JSONArray
        var value = ""
        try {
            val file = File(context?.filesDir, fileName)
            if (file.exists() && file.isFile) {
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
            } else {
                println("File not found")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return value
    }
}