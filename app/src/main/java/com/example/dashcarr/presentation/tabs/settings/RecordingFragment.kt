package com.example.dashcarr.presentation.tabs.settings

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentRecordingBinding
import com.example.dashcarr.presentation.core.BaseFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDateTime


class RecordingFragment : BaseFragment<FragmentRecordingBinding>(
    FragmentRecordingBinding::inflate,
    showBottomNavBar = false
), SensorEventListener {
    private lateinit var bottomNavigationView: BottomNavigationView
    private val viewModel: RecordingViewModel by viewModels()

    private lateinit var sensorManager: SensorManager
    private var accelSensor: Sensor? = null
    private var gyroSensor: Sensor? = null
    private var magnetoSensor: Sensor? = null
    private var isRecording = true

    var elapsedTime = ""


    // Accelerometer
    val rawAccData = FloatArray(3)
    val rawAccDataIndex = 0
    val filtAccData = FloatArray(3)
    val filtAccPrevData = FloatArray(3)
    private val rawAcclRecord = mutableListOf<SensorData>()
    private val filtAcclRecord = mutableListOf<SensorData>()

    var count = 0
    val beginTime = System.nanoTime()
    val rc = 0.002f

    // Gyroscope
    val rawGyroData = FloatArray(3)
    val rawGyroDataIndex = 0
    val filtGyroData = FloatArray(3)
    val filtGyroPrevData = FloatArray(3)
    private val rawGyroRecord = mutableListOf<SensorData>()
    private val filtGyroRecord = mutableListOf<SensorData>()


    // Orientation
    private val accelerometerRading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    }


    private var startTimeMillis: Long = 0
    var handler = Handler()
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateElapsedTime()
            handler.postDelayed(this, 1000)
        }
    }

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

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            recordingFragment = this@RecordingFragment
            buttonStopRecording.setOnClickListener {
                stopRecording()
                findNavController().navigate(R.id.action_action_recording_to_SavedRecordingsFragment)
            }
        }


        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!
        magnetoSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!

    }

    fun updateElapsedTime() {
        val currentTimeMillis = SystemClock.elapsedRealtime()
        val elapsedTimeMillis = currentTimeMillis - startTimeMillis
        val seconds = ((elapsedTimeMillis / 1000) % 60).toInt()
        val minutes = ((elapsedTimeMillis / (1000 * 60)) % 60).toInt()
        val hours = ((elapsedTimeMillis / (1000 * 60 * 60)) % 24).toInt()
        elapsedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        binding.textElapsedTime.text = getString(
            R.string.elapsed_time, elapsedTime
        )
    }


    fun readJsonFromFile(fileName: String): JSONArray {
        var jsonArray = JSONArray()
        try {
            val inputStream = context?.openFileInput(fileName)
            if (inputStream != null) {
                val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
                var line: String? = reader.readLine()
                Log.d("stian", line.toString())

                Log.d("line", line.toString())
                Log.d("lasse", line!!.substring(1, line!!.length - 1))


                jsonArray = JSONArray(line.toString())

                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return jsonArray
    }

    fun saveToCSV() {

        val unfilteredAccelCsvStringBuilder = StringBuilder()
        val filteredAccelCsvStringBuilder = StringBuilder()
        val unfilteredGyroCsvStringBuilder = StringBuilder()
        val filteredGyroCsvStringBuilder = StringBuilder()

        var rawAcclID = 0
        var filtAcclID = 0
        var rawGyroID = 0
        var filtGyroID = 0

        val newRecording = JSONObject()
        unfilteredAccelCsvStringBuilder.append("ID, Accel_Timestamp(ms), Accel_X, Accel_Y, Accel_Z\n")
        filteredAccelCsvStringBuilder.append("ID, Accel_Timestamp(ms), Accel_X, Accel_Y, Accel_Z\n")

        unfilteredGyroCsvStringBuilder.append("ID, Gyro_Timestamp(ms), Gyro_X, Gyro_Y, Gyro_Z\n")
        filteredGyroCsvStringBuilder.append("ID, Gyro_Timestamp(ms), Gyro_X, Gyro_Y, Gyro_Z\n")

        rawAcclRecord.forEach {
            unfilteredAccelCsvStringBuilder.append("$rawAcclID, ${it.timestamp}, ${it.x}, ${it.y}, ${it.z}\n")
            rawAcclID++
        }

        filtAcclRecord.forEach {
            filteredAccelCsvStringBuilder.append("$filtAcclID, ${it.timestamp}, ${it.x}, ${it.y}, ${it.z}\n")
            filtAcclID++
        }

        rawGyroRecord.forEach {
            unfilteredGyroCsvStringBuilder.append("$rawGyroID, ${it.timestamp}, ${it.x}, ${it.y}, ${it.z}\n")
            rawGyroID++
        }

        filtGyroRecord.forEach {
            filteredGyroCsvStringBuilder.append("$filtGyroID, ${it.timestamp}, ${it.x}, ${it.y}, ${it.z}\n")
            filtGyroID++
        }

        val stopDateTime = LocalDateTime.now()

        newRecording.put("name", stopDateTime)
        newRecording.put("elapsed_time", elapsedTime)
        newRecording.put("date", stopDateTime)
        newRecording.put("unfil_gyro", "${stopDateTime}_unfiltered_gyro.csv")
        newRecording.put("fil_gyro", "${stopDateTime}_filtered_gyro.csv")
        newRecording.put("unfil_accel", "${stopDateTime}_unfiltered_accel.csv")
        newRecording.put("fil_accel", "${stopDateTime}_filtered_accel.csv")

        Log.d("newRecording", newRecording.toString())
        val existingJSONArray = readJsonFromFile("sensor_config.json")
        Log.d("jsonArrayTest", existingJSONArray.toString())

        val jsonArray: JSONArray = if (existingJSONArray is JSONArray) {
            existingJSONArray
        } else {
            JSONArray()
        }

        jsonArray.put(newRecording)

        context?.openFileOutput("${stopDateTime}_unfiltered_accel.csv", Context.MODE_PRIVATE).use {
            if (it != null) {
                it.write(unfilteredAccelCsvStringBuilder.toString().toByteArray())
            }
        }

        context?.openFileOutput("${stopDateTime}_filtered_accel.csv", Context.MODE_PRIVATE).use {
            if (it != null) {
                it.write(filteredAccelCsvStringBuilder.toString().toByteArray())
            }
        }

        context?.openFileOutput("sensor_config.json", Context.MODE_PRIVATE).use {
            if (it != null) {
                it.write(jsonArray.toString().toByteArray())
            }
        }
        Log.d("lenJsonArray", existingJSONArray.length().toString())

        context?.openFileOutput("${stopDateTime}_unfiltered_gyro.csv", Context.MODE_PRIVATE).use {
            if (it != null) {
                it.write(unfilteredGyroCsvStringBuilder.toString().toByteArray())
            }
        }

        context?.openFileOutput("${stopDateTime}_filtered_gyro.csv", Context.MODE_PRIVATE).use {
            if (it != null) {
                it.write(filteredGyroCsvStringBuilder.toString().toByteArray())
            }
        }


    }

    override fun onResume() {
        super.onResume()
        startTimeMillis = SystemClock.elapsedRealtime()
        handler.post(updateTimeRunnable)

        sensorManager.registerListener(
            this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST
        )
        sensorManager.registerListener(
            this, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST
        )
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimeRunnable)
        sensorManager.unregisterListener(this)
    }

    fun stopRecording() {
        isRecording = false
        saveToCSV()
        //binding.showRecordStat.visibility = View.VISIBLE
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.values != null) {
            if (event?.sensor == accelSensor) {
                // Unfiltered Accelerometer
                binding.unfilAccelField.text = getString(
                    R.string.accelerometer_unfiltered_template,
                    (event.values[0] + event.values[1] + event.values[2]).toDouble()

                )


                // Filtered Accelerometer
                readAccSensorData(event)
                if (isRecording) {
                    rawAcclRecord.add(
                        SensorData(
                            event.timestamp, event.values[0], event.values[1], event.values[2]
                        )
                    )

                    filtAcclRecord.add(
                        SensorData(event.timestamp, filtAccData[0], filtAccData[1], filtAccData[2])
                    )
                }


            }
            // Unfiltered Gyroscope
            if (event?.sensor == gyroSensor) {
                binding.unfilGyroField.text = getString(
                    R.string.gyroscope_unfiltered_template,

                    (event.values[0] + event.values[1] + event.values[2]).toDouble()


                )
                readGyroSensorData(event)

                if (isRecording) {
                    rawGyroRecord.add(
                        SensorData(
                            event.timestamp, event.values[0], event.values[1], event.values[2]
                        )
                    )
                    filtGyroRecord.add(
                        SensorData(
                            event.timestamp, filtGyroData[0], filtGyroData[1], filtGyroData[2]

                        )
                    )
                }
                // Filtered Gyroscope
            }
            // Orientation
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(
                    event.values, 0, accelerometerRading, 0, accelerometerRading.size
                )
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(
                    event.values, 0, magnetometerReading, 0, magnetometerReading.size
                )
            }
            updateOrientationAngles()

            binding.orientationTextField.text = getString(
                R.string.orientation_tenplate,
                (orientationAngles[0] + orientationAngles[1] + orientationAngles[2])
            )
        }
    }


    fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(
            rotationMatrix, null, accelerometerRading, magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
    }

    private fun readAccSensorData(event: SensorEvent) {
        System.arraycopy(event.values, 0, rawAccData, rawAccDataIndex, 3)
        filterAccData()
        binding.filAccelField.text = getString(
            R.string.accelerometer_filtered_template,
            (filtAccData[0] + filtAccData[1] + filtAccData[2])
        )
    }

    private fun readGyroSensorData(event: SensorEvent) {
        System.arraycopy(event.values, 0, rawGyroData, rawGyroDataIndex, 3)
        filterGyroData()
        binding.filGyroField.text = getString(
            R.string.gyroscope_filtered_template,
            (filtGyroData[0] + filtGyroData[1] + filtGyroData[2])
        )
    }


    private fun filterGyroData() {
        val tm = System.nanoTime()
        val dt = ((tm - beginTime) / 1000000000.0f) / count
        val alpha = rc / (rc + dt)
        val isStarted = true

        if (count == 0) {
            filtGyroPrevData[0] = (1 - alpha) * rawGyroData[0]
            filtGyroPrevData[1] = (1 - alpha) * rawGyroData[1]
            filtGyroPrevData[2] = (1 - alpha) * rawGyroData[2]
        } else {
            filtGyroPrevData[0] = alpha * filtGyroPrevData[0] + (1 - alpha) * rawGyroData[0]
            filtGyroPrevData[1] = alpha * filtGyroPrevData[1] + (1 - alpha) * rawGyroData[1]
            filtGyroPrevData[2] = alpha * filtGyroPrevData[2] + (1 - alpha) * rawGyroData[2]
        }
        if (isStarted) {
            filtGyroData[0] = filtGyroPrevData[0]
            filtGyroData[1] = filtGyroPrevData[1]
            filtGyroData[2] = filtGyroPrevData[2]

        }
    }


    private fun filterAccData() {
        val tm = System.nanoTime()
        val dt = ((tm - beginTime) / 1000000000.0f) / count
        val alpha = rc / (rc + dt)
        val isStarted = true

        if (count == 0) {
            filtAccPrevData[0] = (1 - alpha) * rawAccData[0]
            filtAccPrevData[1] = (1 - alpha) * rawAccData[1]
            filtAccPrevData[2] = (1 - alpha) * rawAccData[2]
        } else {
            filtAccPrevData[0] = alpha * filtAccPrevData[0] + (1 - alpha) * rawAccData[0]
            filtAccPrevData[1] = alpha * filtAccPrevData[1] + (1 - alpha) * rawAccData[1]
            filtAccPrevData[2] = alpha * filtAccPrevData[2] + (1 - alpha) * rawAccData[2]
        }
        if (isStarted) {
            filtAccData[0] = filtAccPrevData[0]
            filtAccData[1] = filtAccPrevData[1]
            filtAccData[2] = filtAccPrevData[2]
        }
        ++count
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("dad", sensor.toString())
    }
}