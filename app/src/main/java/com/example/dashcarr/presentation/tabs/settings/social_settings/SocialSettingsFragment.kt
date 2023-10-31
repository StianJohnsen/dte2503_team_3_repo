package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.dashcarr.R

class SocialSettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SocialSettingsFragment()
    }

    private lateinit var viewModel: SocialSettingsViewModel


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SocialSettingsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_social_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addButton = view.findViewById<Button>(R.id.add_record_button)
        addButton.setOnClickListener {
            viewModel.addToDatabase(requireContext())
        }
    }
}