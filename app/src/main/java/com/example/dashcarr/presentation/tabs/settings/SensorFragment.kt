package com.example.dashcarr.presentation.tabs.settings

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSensorBinding
import com.example.dashcarr.presentation.core.BaseFragment


class SensorFragment : BaseFragment<FragmentSensorBinding>(
    FragmentSensorBinding::inflate
), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelSensor: Sensor? = null
    private var gyroSensor: Sensor? = null
    private var magnetoSensor: Sensor? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    }

    // Starting the listening on all sensors
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            accelSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        sensorManager.registerListener(
            this,
            gyroSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        sensorManager.registerListener(
            this,
            magnetoSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }


    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!
        magnetoSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!



    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.values != null){
        if (event?.sensor == accelSensor) {
                binding.accelUnfiltered.text = getString(R.string.accelerometer_unfiltered_template,event.values[0] + event.values[1] + event.values[2])
            }
            if (event?.sensor == gyroSensor){
                binding.gyroUnfiltered.text = getString(R.string.gyroscope_unfiltered_template,event.values[0] + event.values[1] + event.values[2])

            }
            //if (event?.sensor == magnetoSensor){
            //    Log.d("")
            //}
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("dad", sensor.toString())
    }


}