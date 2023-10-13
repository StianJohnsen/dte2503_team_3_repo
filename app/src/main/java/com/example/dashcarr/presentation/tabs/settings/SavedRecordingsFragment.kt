package com.example.dashcarr.presentation.tabs.settings

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSavedRecordingsBinding
import com.example.dashcarr.presentation.core.BaseFragment
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDateTime
import kotlin.math.absoluteValue
import kotlin.math.min


class SavedRecordingsFragment : BaseFragment<FragmentSavedRecordingsBinding>(
    FragmentSavedRecordingsBinding::inflate,
    showBottomNavBar = false
) {


    enum class CarState {
        ACCELERATING, IDLING, DECELERATING, SHAKING, UNKNOWN
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            savedRecordingsFragment = this@SavedRecordingsFragment
        }
        binding.buttonShowStats.setOnClickListener {
            findNavController().navigate(R.id.action_action_savedrecordings_to_StatisticsFragment)
        }
        binding.imageBackSaved.setOnClickListener {
            findNavController().navigate(R.id.action_action_savedrecordings_to_SettingsFragment)
        }

        createDropdownsFromJson()
    }

    fun readJsonFromFile(): JSONArray {
        var jsonArray = JSONArray()
        val fileName = "sensor_config.json"
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

    private fun createDropdownsFromJson() {
        val jsonArray = readJsonFromFile()
        val linearLayout = view?.findViewById<LinearLayout>(R.id.linear_recordings_buttons)
        linearLayout?.removeAllViews()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val spinner = Spinner(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(30, 30, 30, 5)
                }
                setBackgroundColor(Color.WHITE)
            }

            val options = mapOf(
                jsonObject.getString("name") to "n.A.",
                "unfiltered gyroscope" to jsonObject.getString("unfil_gyro"),
                "filtered gyroscope" to jsonObject.getString("fil_gyro"),
                "unfiltered acceleration" to jsonObject.getString("unfil_accel"),
                "filtered acceleration" to jsonObject.getString("fil_accel")
            ).filterNot { it.value.isEmpty() } as HashMap
            if (jsonObject.has("car_states")) {
                options["open car analysis"] = jsonObject.getString("car_states")
            } else {
                options["analyse car states"] = ""
            }

            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, options.keys.toList()).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = this
            }

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // No action needed
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position == 0) return
                    var filename = options.toList()[position].second
                    if (filename.isEmpty()) {
                        filename =
                            analyseCarStates(options["filtered acceleration"]!!, options["filtered gyroscope"]!!)
                        jsonObject.put("car_states", filename)
                        context?.openFileOutput("sensor_config.json", Context.MODE_PRIVATE).use {
                            it?.write(jsonArray.toString().toByteArray())
                        }
                    }
                    val action =
                        SavedRecordingsFragmentDirections.actionActionSavedrecordingsToRecordingDetailsFragment(
                            filename
                        )
                    findNavController().navigate(action)
                }
            }

            linearLayout?.addView(spinner)
        }
    }

    private fun getListFromFile(file: String): List<List<Float>> {
        val stream: InputStream = requireContext().openFileInput(file)
        val reader = BufferedReader(InputStreamReader(stream, Charset.forName("UTF-8")))
        reader.readLine()
        val lines = reader.readLines().map { line -> line.split(",").map { str -> str.toFloat() } }
        return lines
    }

    private fun median(list: List<Float>) = list.sorted().let {
        if (it.size % 2 == 0)
            (it[it.size / 2] + it[(it.size - 1) / 2]) / 2
        else
            it[it.size / 2]
    }

    private fun lowPassFilter(input: List<Float>, averaging: Int): List<Float> {
        val averagingValues = emptyList<Float>().toMutableList()
        val result = emptyList<Float>().toMutableList()


        var currentIndex = 0
        for (value in input) {
            if (averagingValues.size < averaging) {
                averagingValues.add(value)
            } else {
                averagingValues[currentIndex] = value
                currentIndex += 1
                currentIndex %= averaging
            }
            result.add(median(averagingValues))
        }
        return result
    }

    private fun analyseCarStates(accFilePath: String, gyroFilePath: String): String {
        var accData = getListFromFile(accFilePath).map { it[4] }
        accData = lowPassFilter(accData, 50)

        val gyroData = getListFromFile(gyroFilePath)
        var processedGyroData = List(gyroData.size) { 0F }
        for (i in 2..4) {
            val derivative = gyroData.zipWithNext().map { (it.first[i] - it.second[i]).absoluteValue }
            processedGyroData =
                processedGyroData.zip(lowPassFilter(derivative, 200))
                    .map { it.first + it.second }
        }

        val fileName = "${LocalDateTime.now()}_car_states.csv"
        val fileOutput = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)
        fileOutput.write("car_states\n".toByteArray())

        val relevantAccelerationValues = emptyList<Float>().toMutableList()
        val allCarStates = emptyList<CarState>().toMutableList()
        var lastState = CarState.UNKNOWN
        for (i in 0..<min(accData.size, processedGyroData.size)) {
            val currentState =
                if (lastState == CarState.SHAKING && processedGyroData[i] > 0.03 || processedGyroData[i] > 0.06) {
                    CarState.SHAKING
                } else {
                    relevantAccelerationValues.add(accData[i])
                    CarState.UNKNOWN
                }
            allCarStates.add(currentState)
            lastState = currentState
        }
        val zOffset = median(relevantAccelerationValues)
        for (i in 0..<allCarStates.size) {
            if (allCarStates[i] == CarState.UNKNOWN) {
                val currentAcceleration = accData[i] - zOffset
                allCarStates[i] = when {
                    currentAcceleration > 0.5 -> CarState.ACCELERATING

                    0 < currentAcceleration && currentAcceleration <= 0.5 ->
                        if (i == 0 || allCarStates[i - 1] == CarState.DECELERATING) {
                            CarState.IDLING
                        } else {
                            allCarStates[i - 1]
                        }

                    -0.5 < currentAcceleration && currentAcceleration <= 0 ->
                        if (i == 0 || allCarStates[i - 1] == CarState.ACCELERATING) {
                            CarState.IDLING
                        } else {
                            allCarStates[i - 1]
                        }

                    currentAcceleration <= -0.5 -> CarState.DECELERATING

                    // should never happen
                    else -> CarState.UNKNOWN
                }
            }
            fileOutput.write("${i}, ${gyroData[i][1]}, ${processedGyroData[i]}, ${allCarStates[i].ordinal}\n".toByteArray())
        }
        fileOutput.close()
        return fileName
    }
}
