package com.example.dashcarr.presentation.tabs.settings

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
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
    private var isRecording = false


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
    override fun observeViewModel() {
        TODO("Not yet implemented")
    }

    override fun initListeners() {
        TODO("Not yet implemented")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    }

    // Starting the listening on all sensors
    override fun onResume() {
        super.onResume()
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


    fun saveToCSV() {

        val unfilteredFileName = "unfiltered_sensor_data.csv"
        val filteredFileName = "filtered_sensor_data.csv"

        val unfilteredCsvStringBuilder = StringBuilder()
        val filteredCsvStringBuilder = StringBuilder()

        unfilteredCsvStringBuilder.append("ID, Accel_Timestamp(ms), Accel_X, Accel_Y, Accel_Z, Gyro_Timestamp(ms), Gyro_X, Gyro_Y, Gyro_Z\n")
        filteredCsvStringBuilder.append("ID, Accel_Timestamp(ms), Accel_X, Accel_Y, Accel_Z, Gyro_Timestamp(ms), Gyro_X, Gyro_Y, Gyro_Z\n")

        val unfilteredMaxRecord = maxOf(rawAcclRecord.size, rawGyroRecord.size)
        val filteredMaxRecord = maxOf(filtAcclRecord.size, filtGyroRecord.size)

        for (i in 0 until unfilteredMaxRecord) {
            val accelRecord = if (i < rawAcclRecord.size) rawAcclRecord[i] else null
            val gyroRecord = if (i < rawGyroRecord.size) rawGyroRecord[i] else null

            val id = i + 1

            val accelTimestamp = accelRecord?.timestamp ?: ""
            val accelX = accelRecord?.x ?: ""
            val accelY = accelRecord?.y ?: ""
            val accelZ = accelRecord?.z ?: ""

            val gyroTimestamp = gyroRecord?.timestamp ?: ""
            val gyroX = gyroRecord?.x ?: ""
            val gyroY = gyroRecord?.y ?: ""
            val gyroZ = gyroRecord?.z ?: ""

            unfilteredCsvStringBuilder.append("$id, $accelTimestamp, $accelX, $accelY, $accelZ, $gyroTimestamp, $gyroX, $gyroY, $gyroZ\n")

        }

        for (i in 0 until filteredMaxRecord) {
            val accelRecord = if (i < filtAcclRecord.size) filtAcclRecord[i] else null
            val gyroRecord = if (i < filtGyroRecord.size) filtGyroRecord[i] else null

            val id = i + 1

            val accelTimestamp = accelRecord?.timestamp ?: ""
            val accelX = accelRecord?.x ?: ""
            val accelY = accelRecord?.y ?: ""
            val accelZ = accelRecord?.z ?: ""

            val gyroTimestamp = gyroRecord?.timestamp ?: ""
            val gyroX = gyroRecord?.x ?: ""
            val gyroY = gyroRecord?.y ?: ""
            val gyroZ = gyroRecord?.z ?: ""

            filteredCsvStringBuilder.append("$id, $accelTimestamp, $accelX, $accelY, $accelZ, $gyroTimestamp, $gyroX, $gyroY, $gyroZ\n")
        }

        val unfilteredFileContents = unfilteredCsvStringBuilder.toString()
        val filteredFileContents = filteredCsvStringBuilder.toString()


        //val fileContents = rawAcclRecord.toString()
        context?.openFileOutput(unfilteredFileName, Context.MODE_PRIVATE).use {
            if (it != null) {
                it.write(unfilteredFileContents.toByteArray())
            }
        }

        context?.openFileOutput(filteredFileName, Context.MODE_PRIVATE).use {
            if (it != null) {
                it.write(filteredFileContents.toByteArray())
            }
        }


    }

    override fun onPause() {
        super.onPause()
        Log.d("json", rawAcclRecord.toString())

        sensorManager.unregisterListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!
        magnetoSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
        binding.apply {
            startRecording.setOnClickListener {

                if (!isRecording) {
                    startRecording()
                    binding.startRecording.text = "Stop Recording"
                } else {
                    stopRecording()
                    binding.startRecording.text = "Start Recording"
                }

            }
            /*
            showRecordStat.setOnClickListener {
                moveToStat()
            }
             */

        }


    }

    /*
        fun moveToStat(){
        findNavController().navigate(R.id.action_sensorFragment_to_sensorInfoFragment)
    }
     */


    fun startRecording() {
        isRecording = true
    }

    fun stopRecording() {
        isRecording = false
        saveToCSV()
        binding.showRecordStat.visibility = View.VISIBLE
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.values != null) {
            if (event?.sensor == accelSensor) {
                // Unfiltered Accelerometer
                binding.accelUnfiltered.text = getString(
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
                binding.gyroUnfiltered.text = getString(
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

            binding.orientationText.text = getString(
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
        binding.accelFiltered.text = getString(
            R.string.accelerometer_filtered_template,
            (filtAccData[0] + filtAccData[1] + filtAccData[2])
        )
    }

    private fun readGyroSensorData(event: SensorEvent) {
        System.arraycopy(event.values, 0, rawGyroData, rawGyroDataIndex, 3)
        filterGyroData()
        binding.gyroFiltered.text = getString(
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

