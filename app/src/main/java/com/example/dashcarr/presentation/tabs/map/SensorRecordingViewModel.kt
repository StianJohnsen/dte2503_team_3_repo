package com.example.dashcarr.presentation.tabs.map

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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


class SensorRecordingViewModel(application: Application) : AndroidViewModel(application) {

    val rpmLiveData = RPMLiveData()
    val recordViewModel = RecordingViewModel()

    inner class RPMLiveData : SensorEventListener {

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

        private val _stepNumber = MutableStateFlow(0)

        private val _isBtnStartShowing = MutableStateFlow(0) // 0 == showing, 8 == not showing
        val isBtnStartShowing = _isBtnStartShowing.asStateFlow()

        private val _isBtnStopShowing = MutableStateFlow(8)
        val isBtnStopShowing = _isBtnStopShowing.asStateFlow()

        private val _isBtnPauseShowing = MutableStateFlow(8)
        val isBtnPauseShowing = _isBtnPauseShowing.asStateFlow()

        private val _isBtnResumeShowing = MutableStateFlow(8)
        val isBtnResumeShowing = _isBtnResumeShowing.asStateFlow()

        private val _isBtnDeleteShowing = MutableStateFlow(8)
        val isBtnDeleteShowing = _isBtnDeleteShowing.asStateFlow()


        suspend fun setIsRecording(isRecordingBoolean: Boolean) {
            _isRecording.emit(isRecordingBoolean)
        }

        fun makeStartJsonObject(elapsedTime: String): JSONObject {
            val startJsonObject = JSONObject()
            val stopDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

            startJsonObject.put("name", "Recording from ${stopDateTime.format(formatter)}")
            startJsonObject.put("elapsed_time", elapsedTime)
            startJsonObject.put("date", stopDateTime)

            return startJsonObject

        }

        fun makeJsonObject(
            dateTime: LocalDateTime,
            filtered: String,
            sensor: String,
            jsonObject: JSONObject
        ): JSONObject {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")

            jsonObject.put(
                "${filtered}_${sensor}",
                "${dateTime.format(formatter)}_${filtered}tered_${sensor}.csv"
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

        //fun readJsonFromFile()

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

        suspend fun writeToJsonFile(context: Context, currentJsonArray: JSONArray, newJsonObjects: JSONObject) {
            currentJsonArray.put(newJsonObjects)
            withContext(Dispatchers.IO) {
                context.openFileOutput("sensor_config.json", Context.MODE_PRIVATE).use {
                    it?.write(currentJsonArray.toString().toByteArray())
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
            // NOT USED
        }
    }
}
