package com.example.dashcarr.presentation.tabs.history

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentHistoryBinding
import com.example.dashcarr.presentation.core.BaseFragment
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
import kotlin.math.min

/**
 * HistoryFragment manages the display and interactions in the history tab.
 */
class HistoryFragment : BaseFragment<FragmentHistoryBinding>(
    FragmentHistoryBinding::inflate,
    showBottomNavBar = true
) {

    private data class RecordingDescription(val label: String, var fileName: String, val chartType: String = "line") {
        fun exists(): Boolean = fileName.isNotEmpty()
    }

    enum class CarState(val color: Int) {
        ACCELERATING(
            Color.rgb(
                4,
                47,
                102
            )
        ),
        IDLING(Color.rgb(185, 116, 85)), DECELERATING(
            Color.rgb(
                224,
                123,
                57
            )
        ),
        SHAKING(Color.rgb(228, 57, 30)), UNKNOWN(Color.TRANSPARENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            historyFragment = this@HistoryFragment
        }
        binding.buttonShowStats.setOnClickListener {
            showBottomNavigation(false)
            findNavController().navigate(R.id.action_action_history_to_StatisticsFragment)
        }

        createDropdownsFromJson()
    }

    /**
     * Reads the sensor_config.json file and returns its content as a JSONArray.
     */
    private fun readJsonFromFile(): JSONArray {
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

    private fun deleteRecording(index: Int, jsonArray: JSONArray) {
        jsonArray.remove(index)
        context?.openFileOutput("sensor_config.json", Context.MODE_PRIVATE).use {
            it?.write(jsonArray.toString().toByteArray())
        }
        createDropdownsFromJson()
    }

    private fun renameRecording(index: Int, jsonArray: JSONArray, newName: String) {
        val jsonObject = jsonArray.getJSONObject(index)
        jsonObject.put("name", newName)
        context?.openFileOutput("sensor_config.json", Context.MODE_PRIVATE).use {
            it?.write(jsonArray.toString().toByteArray())
        }
        createDropdownsFromJson()
    }

    private fun showRenameDialog(index: Int, jsonArray: JSONArray) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Rename the recording")

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val newName = input.text.toString()
            if (newName.isNotEmpty()) {
                renameRecording(index, jsonArray, newName)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun createDropdownsFromJson() {
        val jsonArray = readJsonFromFile()
        val linearLayout = view?.findViewById<LinearLayout>(R.id.linear_recordings_buttons)
        linearLayout?.removeAllViews()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val horizontalLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(30, 30, 30, 5)
                }
                orientation = LinearLayout.HORIZONTAL
            }

            val spinner = Spinner(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                setPopupBackgroundResource(R.drawable.white_rounded_16dp_background)
            }

            val options = listOf(
                RecordingDescription(jsonObject.getString("name"), "n.A."),
                RecordingDescription("unfiltered gyroscope", jsonObject.getString("unfil_gyro")),
                RecordingDescription("filtered gyroscope", jsonObject.getString("fil_gyro")),
                RecordingDescription("unfiltered acceleration", jsonObject.getString("unfil_accel")),
                RecordingDescription("filtered acceleration", jsonObject.getString("fil_accel"))
            ).filter { it.exists() }.toMutableList()

            if (jsonObject.has("car_states")) {
                options.add(RecordingDescription("open car analysis", jsonObject.getString("car_states"), "bar"))
            } else {
                options.add(RecordingDescription("open car analysis", "", "bar"))
            }

            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options.map { it.label }).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = this
            }
            spinner.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.text_color));
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // No action needed
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position == 0) return
                    if (!options[position].exists()) {
                        val newName =
                            analyseCarStates(
                                options.first() { it.label == "filtered acceleration" }.fileName,
                                options.first() { it.label == "filtered gyroscope" }.fileName
                            )
                        jsonObject.put("car_states", newName)
                        context?.openFileOutput("sensor_config.json", Context.MODE_PRIVATE).use {
                            it?.write(jsonArray.toString().toByteArray())
                        }
                        options[position].fileName = newName
                    }
                    val action =
                        HistoryFragmentDirections.actionActionHistoryToRecordingDetailsFragment(
                            selectedFileName = options[position].fileName,
                            elapsedTime = jsonObject.getString("elapsed_time"),
                            title = jsonObject.getString("name"),
                            chartType = options[position].chartType,
                            date = jsonObject.getString("date")
                        )
                    findNavController().navigate(action)
                }
            }
            horizontalLayout.addView(spinner)

            val buttonLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val renameButton = Button(context).apply {
                text = "Rename"
                setBackgroundResource(R.drawable.orange_button)
                setTextColor(Color.WHITE)
                setOnClickListener {
                    showRenameDialog(i, jsonArray)
                }
            }

            val deleteButton = Button(context).apply {
                text = "Delete"
                setBackgroundResource(R.drawable.red_button)
                setTextColor(Color.WHITE)
                setOnClickListener {
                    deleteRecording(i, jsonArray)
                }
            }

            val buttonWidth = 150
            val buttonHeight = 100

            val renameParams = LinearLayout.LayoutParams(buttonWidth, buttonHeight)
            renameParams.setMargins(0, 0, 16, 0)

            val deleteParams = LinearLayout.LayoutParams(buttonWidth, buttonHeight)

            renameButton.layoutParams = renameParams
            deleteButton.layoutParams = deleteParams

            buttonLayout.addView(renameButton)
            buttonLayout.addView(deleteButton)

            buttonLayout.gravity = Gravity.CENTER

            horizontalLayout.addView(buttonLayout)
            linearLayout?.addView(horizontalLayout)
        }
    }

    private fun readValuesFromFile(file: String): List<List<Float>> {
        val stream: InputStream = requireContext().openFileInput(file)
        val reader = BufferedReader(InputStreamReader(stream, Charset.forName("UTF-8")))
        reader.readLine()
        return reader.readLines().map { line -> line.split(",").map { str -> str.toFloat() } }
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
        var accData = readValuesFromFile(accFilePath).map { it[4] }
        accData = lowPassFilter(accData, 50)

        val gyroData = readValuesFromFile(gyroFilePath)
        var processedGyroData = List(gyroData.size) { 0F }
        for (i in 2..4) {
            val derivative = gyroData.zipWithNext().map { (it.first[i] - it.second[i]).absoluteValue }
            processedGyroData =
                processedGyroData.zip(lowPassFilter(derivative, 100))
                    .map { it.first + it.second }
        }
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")
        val fileName = "${LocalDateTime.now().format(formatter)}_car_states.csv"
        val fileOutput = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)
        fileOutput.write("ID, Gyro_Timestamp(ms), Car_States\n".toByteArray())

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
                val currentAcceleration = zOffset - accData[i]
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
            fileOutput.write("${i},${gyroData[i][1]},${allCarStates[i]}\n".toByteArray())
        }
        fileOutput.close()
        return fileName
    }
}
