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
import java.lang.Math.min
import java.nio.charset.Charset
import java.time.LocalDateTime
import kotlin.math.abs


class SavedRecordingsFragment : BaseFragment<FragmentSavedRecordingsBinding>(
    FragmentSavedRecordingsBinding::inflate,
    showBottomNavBar = false
) {

    enum class CarState {
        ACCELERATING, IDLING, DECELERATING, UNKNOWN
    }

    private val viewModel: SavedRecordingsViewModel by viewModels()
    override fun observeViewModel() {
        TODO("Not yet implemented")
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
                "filtered acceleration" to jsonObject.getString("fil_accel"),
                "filtered acceleration" to jsonObject.getString("fil_accel")
            ).filterNot { it.value.isEmpty() } as HashMap
            if(jsonObject.has("car_states")){
                options.put("open car analysis", jsonObject.getString("car_states"))
            } else {
                options.put("analyse car states", "")
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
                    if(position == 0) return
                    var filename = options.toList()[position].second
                    if (filename.isEmpty()) {
                        val accFile = options.getOrDefault("filtered acceleration", options["unfiltered acceleration"])
                        val gyroFile = options.getOrDefault("filtered gyroscope", options["unfiltered gyroscope"])
                        filename = analyseCarStates(accFile, gyroFile)
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
        val lines = reader.readLines().map { line -> line.split(",").map { str->str.toFloat() } }
        return lines
    }
    private fun analyseCarStates(accFilePath: String, gyroFilePath: String): String {
        val accData = getListFromFile(accFilePath)
        val gyroData = getListFromFile(gyroFilePath)
        val fileName = "${LocalDateTime.now()}_car_states.csv"
        val fileOutput = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)
        fileOutput.write("car_states\n".toByteArray())
        for (i in 0 .. min(accData.size, gyroData.size) - 1) {
            val currentState = when {
                gyroData[i].subList(2,5).map { abs(it) }.sum() > 0.3 -> CarState.UNKNOWN
                accData[i][4] > 0.4 -> CarState.ACCELERATING
                accData[i][4] < -0.4 -> CarState.DECELERATING
                else -> CarState.IDLING
            }
            fileOutput.write("${i}, ${accData[i][1]}, ${currentState.ordinal}\n".toByteArray())
        }
        fileOutput.close()
        return fileName
    }
}
