package com.example.dashcarr.presentation.tabs.map

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.R
import com.example.dashcarr.presentation.tabs.map.data.SensorData
import com.example.dashcarr.presentation.tabs.settings.PowerSavingMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * ViewModel for managing sensor recordings in the application.
 * It handles the recording of various sensor data and location updates.
 * This ViewModel extends AndroidViewModel to access application context for file operations.
 *
 * The ViewModel utilizes a nested class, `SensorRecording`, for handling sensor events and managing the sensor data lists.
 * It also incorporates a `TimerController` for managing the recording's duration.
 *
 * @property sensorRecording An instance of `SensorRecording` to manage sensor data recording and processing.
 * @property timerController A `TimerController` instance to handle the timing aspects of recording.
 *
 * @constructor Creates a new instance of SensorRecordingViewModel with the given application context.
 */
class SensorRecordingViewModel(application: Application) : AndroidViewModel(application) {

    val sensorRecording = SensorRecording()
    private val timerController = TimerController()
    private val googleOfficeLocation = Location(LocationManager.GPS_PROVIDER).apply {
        latitude = 37.33042
        longitude = -122.084984
    }

    fun startRecording() {
        viewModelScope.launch {
            timerController.startRecording()
        }
        viewModelScope.launch {
            sensorRecording.setIsRecording(true)
        }

    }

    fun stopRecording(context: Context) {
        val currentTime = LocalDateTime.now()
        viewModelScope.launch {
            timerController.stopRecording()
        }
        viewModelScope.launch {
            sensorRecording.setIsRecording(false)
        }
        viewModelScope.launch {
            val startJSONObject = makeStartJsonObject(currentTime)
            val currentSessionJsonObject = makeJsonObject(currentTime, startJSONObject)
            val existingJsonArray = sensorRecording.readJsonFromFile(context)
            if (sensorRecording.unfilteredLocationList.value.isNotEmpty()) {
                sensorRecording.writeToJsonFile(context, existingJsonArray, currentSessionJsonObject)
            }
        }
        tryCatchSensorStringBuilder(context) {
            val unfilteredGPSStringBuilder =
                sensorRecording.buildSensorStringBuilder(sensorRecording.unfilteredLocationList.value)
            sensorRecording.saveToFile(context, unfilteredGPSStringBuilder, "unfiltered", "GPS", currentTime)
        }

        tryCatchSensorStringBuilder(context) {
            val unfilteredAccelerometerStringBuilder =
                sensorRecording.buildSensorStringBuilder(sensorRecording.unfilteredAccelerometerList.value)
            sensorRecording.saveToFile(
                context,
                unfilteredAccelerometerStringBuilder,
                "unfiltered",
                "accel",
                currentTime
            )
        }

        tryCatchSensorStringBuilder(context) {
            val filteredAccelerometerStringBuilder =
                sensorRecording.buildSensorStringBuilder(sensorRecording.filteredAccelerometerList.value)
            sensorRecording.saveToFile(
                context,
                filteredAccelerometerStringBuilder,
                "filtered",
                "accel",
                currentTime
            )
        }

        tryCatchSensorStringBuilder(context) {
            val unfilteredGyroscopeStringBuilder =
                sensorRecording.buildSensorStringBuilder(sensorRecording.unfilteredGyroscopeList.value)
            sensorRecording.saveToFile(context, unfilteredGyroscopeStringBuilder, "unfiltered", "gyro", currentTime)
        }

        tryCatchSensorStringBuilder(context) {
            val filteredGyroscopeStringBuilder =
                sensorRecording.buildSensorStringBuilder(sensorRecording.filteredGyroScopeList.value)
            sensorRecording.saveToFile(context, filteredGyroscopeStringBuilder, "filtered", "gyro", currentTime)
        }



        tryCatchSensorStringBuilder(context) {
            changeButtonWhenClickingStop()
        }
    }

    private fun tryCatchSensorStringBuilder(context: Context, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    context.resources.getString(R.string.not_enough_duration_gps),
                    Toast.LENGTH_LONG
                ).show()
                Log.e(this::class.simpleName, e.stackTraceToString())
            }
        }
    }


    private fun makeStartJsonObject(localDateTime: LocalDateTime) =
        sensorRecording.makeStartJsonObject(localDateTime, timerController.getElapsedTime())

    private fun makeJsonObject(localDateTime: LocalDateTime, jsonObject: JSONObject) =
        sensorRecording.makeJsonObject(localDateTime, jsonObject)

    fun pauseRecording() {
        viewModelScope.launch {
            timerController.pauseRecording()
        }
        viewModelScope.launch {
            sensorRecording.setIsRecording(false)
        }
        viewModelScope.launch {
            sensorRecording.setIsPaused(true)
        }
        viewModelScope.launch {
            changeButtonsWhenClickingPause()
        }

    }

    private suspend fun changeButtonsWhenClickingPause() {
        sensorRecording.setIsRecordingButtonsShowing("Pause", false)
        sensorRecording.setIsRecordingButtonsShowing("Resume", true)
    }

    private suspend fun changeButtonsWhenClickingResume() {
        sensorRecording.setIsRecordingButtonsShowing("Resume", false)
        sensorRecording.setIsRecordingButtonsShowing("Pause", true)
    }

    private suspend fun changeButtonWhenClickingStop() {
        sensorRecording.setIsRecordingButtonsShowing("Stop", false)
        sensorRecording.setIsRecordingButtonsShowing("Start", true)
        sensorRecording.setIsRecordingButtonsShowing("Pause", false)
        sensorRecording.setIsRecordingButtonsShowing("Delete", false)
        sensorRecording.setIsRecordingButtonsShowing("Resume", false)
    }

    private suspend fun changeButtonWhenClickingDelete() {
        sensorRecording.setIsRecordingButtonsShowing("Delete", false)
        sensorRecording.setIsRecordingButtonsShowing("Pause", false)
        sensorRecording.setIsRecordingButtonsShowing("Stop", false)
        sensorRecording.setIsRecordingButtonsShowing("Start", true)
        sensorRecording.setIsRecordingButtonsShowing("Resume", false)

    }

    fun resumeRecording() {
        viewModelScope.launch {
            timerController.resumeRecording()
        }
        viewModelScope.launch {
            sensorRecording.setIsRecording(true)
        }
        viewModelScope.launch {
            sensorRecording.setIsPaused(false)
        }
        viewModelScope.launch {
            changeButtonsWhenClickingResume()
        }
    }

    fun deleteRecording() {
        viewModelScope.launch {
            timerController.stopRecording()
        }
        viewModelScope.launch {
            sensorRecording.setIsRecording(false)
        }
        viewModelScope.launch {
            changeButtonWhenClickingDelete()
        }
    }

    inner class SensorRecording : SensorEventListener {

        private val sensorManager
            get() = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager


        private val _unfilteredLocationList = MutableStateFlow<MutableList<SensorData>>(mutableListOf())
        val unfilteredLocationList = _unfilteredLocationList.asStateFlow()

        private val _unfilteredAccelerometerList = MutableStateFlow<MutableList<SensorData>>(mutableListOf())
        val unfilteredAccelerometerList = _unfilteredAccelerometerList.asStateFlow()

        private val _filteredAccelerometerList = MutableStateFlow<MutableList<SensorData>>(mutableListOf())
        val filteredAccelerometerList = _filteredAccelerometerList.asStateFlow()

        private val _unfilteredGyroscopeList = MutableStateFlow<MutableList<SensorData>>(mutableListOf())
        val unfilteredGyroscopeList = _unfilteredGyroscopeList.asStateFlow()

        private val _filteredGyroScopeList = MutableStateFlow<MutableList<SensorData>>(mutableListOf())
        val filteredGyroScopeList = _filteredGyroScopeList.asStateFlow()

        private val _isRecording = MutableStateFlow(false)
        val isRecording = _isRecording.asStateFlow()

        private val _isPaused = MutableStateFlow(false)
        val isPaused = _isPaused.asStateFlow()

        private val _stepNumber = MutableStateFlow(0)

        private val _isBtnStartShowing = MutableStateFlow(8) // 0 == showing, 8 == not showing

        private val _isBtnStopShowing = MutableStateFlow(0)
        val isBtnStopShowing = _isBtnStopShowing.asStateFlow()

        private val _isBtnPauseShowing = MutableStateFlow(0)
        val isBtnPauseShowing = _isBtnPauseShowing.asStateFlow()

        private val _isBtnResumeShowing = MutableStateFlow(8)
        val isBtnResumeShowing = _isBtnResumeShowing.asStateFlow()

        private val _isBtnDeleteShowing = MutableStateFlow(0)
        val isBtnDeleteShowing = _isBtnDeleteShowing.asStateFlow()

        suspend fun setIsRecording(isRecordingBoolean: Boolean) {
            _isRecording.emit(isRecordingBoolean)
        }

        suspend fun setIsPaused(isPausedBoolean: Boolean) {
            _isPaused.emit(isPausedBoolean)
        }

        fun makeStartJsonObject(localDateTime: LocalDateTime, elapsedTime: String): JSONObject {
            val startJsonObject = JSONObject()

            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

            startJsonObject.put("name", "Recording from ${localDateTime.format(formatter)}")
            startJsonObject.put("elapsed_time", elapsedTime)
            startJsonObject.put("date", localDateTime)

            return startJsonObject

        }

        fun makeJsonObject(
            dateTime: LocalDateTime,
            jsonObject: JSONObject
        ): JSONObject {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")

            jsonObject.put(
                "unfil_GPS",
                "${dateTime.format(formatter)}_unfiltered_GPS.csv"
            )
            jsonObject.put(
                "unfil_accel",
                "${dateTime.format(formatter)}_unfiltered_accel.csv"
            )
            jsonObject.put(
                "fil_accel",
                "${dateTime.format(formatter)}_filtered_accel.csv"
            )
            jsonObject.put(
                "unfil_gyro",
                "${dateTime.format(formatter)}_unfiltered_gyro.csv"
            )
            jsonObject.put(
                "fil_gyro",
                "${dateTime.format(formatter)}_filtered_gyro.csv"
            )
            return jsonObject
        }

        suspend fun setIsRecordingButtonsShowing(kindOfButton: String, isButtonShowing: Boolean) {
            val isButtonShowingInt: Int = when (isButtonShowing) {
                true -> 0
                else -> 8
            }
            when (kindOfButton) {
                "Start" -> _isBtnStartShowing.emit(isButtonShowingInt)
                "Stop" -> _isBtnStopShowing.emit(isButtonShowingInt)
                "Pause" -> _isBtnPauseShowing.emit(isButtonShowingInt)
                "Resume" -> _isBtnResumeShowing.emit(isButtonShowingInt)
                "Delete" -> _isBtnDeleteShowing.emit(isButtonShowingInt)
            }
        }

        private fun addToUnfilteredSensorList(kindOfSensor: String, sensorEvent: SensorEvent) {
            if (kindOfSensor == "Accelerometer") {
                _unfilteredAccelerometerList.value.add(
                    SensorData(
                        kindOfSensor,
                        sensorEvent.timestamp,
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]
                    )

                )
            } else {
                _unfilteredGyroscopeList.value.add(
                    SensorData(
                        kindOfSensor,
                        sensorEvent.timestamp,
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]
                    )
                )

            }

        }

        private fun addToFilteredSensorList(
            kindOfSensor: String,
            sensorEvent: SensorEvent,
            filteredSampleData: FloatArray
        ) {
            if (kindOfSensor == "Accelerometer") {
                _filteredAccelerometerList.value.add(
                    SensorData(
                        kindOfSensor,
                        sensorEvent.timestamp,
                        filteredSampleData[0],
                        filteredSampleData[1],
                        filteredSampleData[2]
                    )
                )

            } else {
                _filteredGyroScopeList.value.add(
                    SensorData(
                        kindOfSensor,
                        sensorEvent.timestamp,
                        filteredSampleData[0],
                        filteredSampleData[1],
                        filteredSampleData[2]
                    )
                )

            }
        }

        private fun setUpUnfilteredDataSample(event: SensorEvent): FloatArray {
            val unfilteredAccelerometerSample = FloatArray(3)
            System.arraycopy(event.values, 0, unfilteredAccelerometerSample, 0, 3)
            return unfilteredAccelerometerSample
        }

        fun insertIntoLocationList(event: Location) {
            if (googleOfficeLocation.distanceTo(event) < 50000) {
                return
            }
            _unfilteredLocationList.value.add(
                SensorData(
                    "GPS",
                    event.time,
                    event.longitude.toFloat(),
                    event.latitude.toFloat(),
                    event.altitude.toFloat()
                )
            )
        }

        private fun filterDataList(rawSampleData: FloatArray): FloatArray {
            val alpha = 0.9F
            val isStarted = true

            val filteredDataPrevSample = FloatArray(3)
            val filteredDataSample = FloatArray(3)

            if (_stepNumber.value == 0) {
                filteredDataPrevSample[0] = rawSampleData[0]
                filteredDataPrevSample[1] = rawSampleData[1]
                filteredDataPrevSample[2] = rawSampleData[2]
            } else {
                filteredDataPrevSample[0] =
                    alpha * filteredDataPrevSample[0] + (1 - alpha) * rawSampleData[0]
                filteredDataPrevSample[1] =
                    alpha * filteredDataPrevSample[1] + (1 - alpha) * rawSampleData[1]
                filteredDataPrevSample[2] =
                    alpha * filteredDataPrevSample[2] + (1 - alpha) * rawSampleData[2]
            }

            if (isStarted) {
                filteredDataSample[0] = filteredDataPrevSample[0]
                filteredDataSample[1] = filteredDataPrevSample[1]
                filteredDataSample[2] = filteredDataPrevSample[2]
            }

            viewModelScope.launch {
                incStepNum()
            }

            return filteredDataSample
        }

        suspend fun saveToFile(
            context: Context,
            stringBuilder: StringBuilder,
            filtered: String,
            sensor: String,
            dateTime: LocalDateTime
        ) {
            withContext(Dispatchers.IO) {
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")
                context.openFileOutput("${dateTime.format(formatter)}_${filtered}_${sensor}.csv", Context.MODE_PRIVATE)
                    .use {
                        it?.write(stringBuilder.toString().toByteArray())
                    }

            }

        }

        private suspend fun incStepNum() {
            _stepNumber.emit(_stepNumber.value + 1)
        }

        override fun onSensorChanged(event: SensorEvent?) {
            var kindOfSensor = ""

            if (event != null) {
                when (event.sensor) {
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) -> kindOfSensor = "Accelerometer"
                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) -> kindOfSensor = "GyroScope"
                }
                if (_isRecording.value) {
                    val rawSampleDataParameter = setUpUnfilteredDataSample(event)
                    val filteredSampleData = filterDataList(rawSampleDataParameter)
                    addToFilteredSensorList(kindOfSensor, event, filteredSampleData)
                    addToUnfilteredSensorList(kindOfSensor, event)
                }
            }
        }

        suspend fun readJsonFromFile(context: Context): JSONArray = withContext(Dispatchers.IO) {
            try {
                val inputStream = context.openFileInput("sensor_config.json")
                val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
                val line: String? = reader.readLine()
                val jsonArray = JSONArray(line.orEmpty())
                inputStream.close()

                return@withContext jsonArray
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle the exception or return a default value if needed
                return@withContext JSONArray()
            }
        }

        fun writeToJsonFile(context: Context, currentJsonArray: JSONArray, newJsonObjects: JSONObject) {
            currentJsonArray.put(newJsonObjects)
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    context.openFileOutput("sensor_config.json", Context.MODE_PRIVATE).use {
                        it?.write(currentJsonArray.toString().toByteArray())
                    }
                }
            }

        }

        fun buildSensorStringBuilder(sensorList: MutableList<SensorData>): StringBuilder {

            val stringBuilder = StringBuilder()
            var id = 0

            val kindOfSensor = sensorList[0].typeOfSensor

            if (sensorList[0].typeOfSensor == "GPS") {
                stringBuilder.append("ID, ${kindOfSensor}_Timestamp(ms), ${kindOfSensor}_Longitude, ${kindOfSensor}_Latitude, ${kindOfSensor}_Altitude\n")
            } else {
                stringBuilder.append("ID, ${kindOfSensor}_Timestamp(ms), ${kindOfSensor}_X, ${kindOfSensor}_Y, ${kindOfSensor}_Z\n")

            }


            sensorList.forEach {
                stringBuilder.append("$id, ${it.timestamp}, ${it.x}, ${it.y}, ${it.z}\n")
                id++
            }

            return stringBuilder

        }

        fun registerSensors() {
            val sensorSamplingRate: Int = if (PowerSavingMode.getPowerMode()) {
                SensorManager.SENSOR_DELAY_NORMAL
            } else {
                SensorManager.SENSOR_DELAY_FASTEST
            }
            sensorManager.let { sm ->
                sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE).let {
                    sm.registerListener(this, it, sensorSamplingRate)
                }
                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).let {
                    sm.registerListener(this, it, sensorSamplingRate)
                }
            }
        }

        fun unregisterSensors() {
            sensorManager.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }
}
