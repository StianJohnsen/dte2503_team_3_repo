package com.example.dashcarr.presentation.tabs.history

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentHistoryBinding
import com.example.dashcarr.presentation.core.BaseFragment
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

/**
 * HistoryFragment manages the display and interactions in the history tab.
 */
class HistoryFragment : BaseFragment<FragmentHistoryBinding>(
    FragmentHistoryBinding::inflate,
    showBottomNavBar = true
) {
    private val viewModel: HistoryViewModel by viewModels()

    /**
     * TODO: Implement to observe ViewModel changes.
     */
    override fun observeViewModel() {
        TODO("Not yet implemented")
    }

    /**
     * TODO: Implement to initialize UI event listeners.
     */
    override fun initListeners() {
        TODO("Not yet implemented")
    }

    /**
     * Setup UI after the view is created.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            historyFragment = this@HistoryFragment
        }

        // Navigate to StatisticsFragment when the button is clicked
        binding.buttonShowStats.setOnClickListener {
            findNavController().navigate(R.id.action_action_history_to_StatisticsFragment)
        }

        // Create dropdowns based on the JSON file
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

    /**
     * Creates dropdown menus from the JSON array.
     */
    private fun createDropdownsFromJson() {
        val jsonArray = readJsonFromFile()
        val linearLayout = view?.findViewById<LinearLayout>(R.id.linear_recordings_buttons)
        linearLayout?.removeAllViews()

        // Loop through each JSON object and create a Spinner
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

            // Create a list of available options, only add non-empty values
            val options = mutableListOf(jsonObject.getString("name"))

            if (jsonObject.getString("unfil_gyro").isNotEmpty()) options.add(jsonObject.getString("unfil_gyro"))
            if (jsonObject.getString("fil_gyro").isNotEmpty()) options.add(jsonObject.getString("fil_gyro"))
            if (jsonObject.getString("unfil_accel").isNotEmpty()) options.add(jsonObject.getString("unfil_accel"))
            if (jsonObject.getString("fil_accel").isNotEmpty()) options.add(jsonObject.getString("fil_accel"))

            // Attach adapter to Spinner
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, options).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = this
            }

            // Listen for selection events on the Spinner
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // No action needed
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position > 0) {
                        val selectedFile = options[position]
                        val action =
                            HistoryFragmentDirections.actionActionHistoryToRecordingDetailsFragment(
                                selectedFile
                            )
                        findNavController().navigate(action)
                    }
                }
            }

            // Add the Spinner to the layout
            linearLayout?.addView(spinner)
        }
    }
}
