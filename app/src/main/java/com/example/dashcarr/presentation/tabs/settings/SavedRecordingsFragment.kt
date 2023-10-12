package com.example.dashcarr.presentation.tabs.settings

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
import com.example.dashcarr.databinding.FragmentSavedRecordingsBinding
import com.example.dashcarr.presentation.core.BaseFragment
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

class SavedRecordingsFragment : BaseFragment<FragmentSavedRecordingsBinding>(
    FragmentSavedRecordingsBinding::inflate,
    showBottomNavBar = false
) {
    private val viewModel: SavedRecordingsViewModel by viewModels()
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

    public fun readJsonFromFile(): JSONArray {
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

            val options = listOf(
                jsonObject.getString("name"),
                jsonObject.getString("unfil_gyro"),
                jsonObject.getString("fil_gyro"),
                jsonObject.getString("unfil_accel"),
                jsonObject.getString("fil_accel")
            )

            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, options).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = this
            }

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // No action needed
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position > 0) {
                        val selectedFile = options[position]
                        val action =
                            SavedRecordingsFragmentDirections.actionActionSavedrecordingsToRecordingDetailsFragment(
                                selectedFile
                            )
                        findNavController().navigate(action)
                    }
                }
            }

            linearLayout?.addView(spinner)
        }
    }
}
