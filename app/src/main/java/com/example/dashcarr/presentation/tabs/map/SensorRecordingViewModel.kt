package com.example.dashcarr.presentation.tabs.map

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.dashcarr.presentation.tabs.map.data.SensorData2
import com.example.dashcarr.presentation.tabs.settings.PowerSavingMode

class SensorRecordingViewModel(application: Application) : AndroidViewModel(application) {

    val rpmLiveData = RPMLiveData()

    inner class RPMLiveData : SensorEventListener {
        private val sensorManager
            get() = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager


        private val _unfilteredLocationList = MutableLiveData<MutableList<SensorData2>>(mutableListOf())
        val unfilteredLocationList: LiveData<MutableList<SensorData2>> = _unfilteredLocationList


        private val _unfilteredAccelerometerList = MutableLiveData<MutableList<SensorData2>>(mutableListOf())
        val unfilteredAccelerometerList: LiveData<MutableList<SensorData2>> = _unfilteredAccelerometerList

        private val _filteredAccelerometerList = MutableLiveData<MutableList<SensorData2>>(mutableListOf())
        val filteredAccelerometerList: LiveData<MutableList<SensorData2>> = _filteredAccelerometerList

        private val _unfilteredGyroscopeList = MutableLiveData<MutableList<SensorData2>>(mutableListOf())
        val unfilteredGyroscopeList: LiveData<MutableList<SensorData2>> = _unfilteredGyroscopeList

        private val _filteredGyroScopeList = MutableLiveData<MutableList<SensorData2>>(mutableListOf())
        val filteredGyroScopeList: LiveData<MutableList<SensorData2>> = _filteredGyroScopeList

        private val _isRecording = MutableLiveData<Boolean>()
        val isRecording: LiveData<Boolean> = _isRecording

        private val _stepNumber = MutableLiveData<Int>()

        private val _isBtnStartShowing = MutableLiveData<Int>()
        val isBtnStartShowing: LiveData<Int> = _isBtnStartShowing

        private val _isBtnStopShowing = MutableLiveData<Int>()
        val isBtnStopShowing: LiveData<Int> = _isBtnStopShowing

        private val _isBtnPauseShowing = MutableLiveData<Int>()
        val isBtnPauseShowing: LiveData<Int> = _isBtnPauseShowing

        private val _isBtnResumeShowing = MutableLiveData<Int>()
        val isBtnResumeShowing: LiveData<Int> = _isBtnResumeShowing

        private val _isBtnDeleteShowing = MutableLiveData<Int>()
        val isBtnDeleteShowing: LiveData<Int> = _isBtnDeleteShowing


        fun setIsRecording(isRecordingBoolean: Boolean) {
            _isRecording.postValue(isRecordingBoolean)
        }


        fun setIsRecordingButtonsShowing(kindOfButton: String, isButtonShowing: Boolean) {
            val isButtonShowingInt: Int
            when (isButtonShowing) {
                true -> isButtonShowingInt = 0
                else -> isButtonShowingInt = 8
            }
            when (kindOfButton) {
                "Start" -> _isBtnStartShowing.postValue(isButtonShowingInt)
                "Stop" -> _isBtnStopShowing.postValue(isButtonShowingInt)
                "Pause" -> _isBtnPauseShowing.postValue(isButtonShowingInt)
                "Resume" -> _isBtnResumeShowing.postValue(isButtonShowingInt)
                "Delete" -> _isBtnDeleteShowing.postValue(isButtonShowingInt)
            }
        }

        fun addToUnfilteredSensorList(kindOfSensor: String, sensorEvent: SensorEvent) {
            if (kindOfSensor == "Accelerometer") {
                _unfilteredAccelerometerList.value?.add(
                    SensorData2(
                        kindOfSensor,
                        sensorEvent.timestamp,
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]
                    )
                )

            } else {
                _unfilteredGyroscopeList.value?.add(
                    SensorData2(
                        kindOfSensor,
                        sensorEvent.timestamp,
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]
                    )
                )

            }

        }

        fun addToFilteredSensorList(kindOfSensor: String, sensorEvent: SensorEvent, filteredSampleData: FloatArray) {
            if (kindOfSensor == "Accelerometer") {
                _filteredAccelerometerList.value?.add(
                    SensorData2(
                        kindOfSensor,
                        sensorEvent.timestamp,
                        filteredSampleData[0],
                        filteredSampleData[1],
                        filteredSampleData[2]
                    )
                )

            } else {
                _filteredGyroScopeList.value?.add(
                    SensorData2(
                        kindOfSensor,
                        sensorEvent.timestamp,
                        filteredSampleData[0],
                        filteredSampleData[1],
                        filteredSampleData[2]
                    )
                )

            }
        }

        fun setUpUnfilteredDataSample(event: SensorEvent): FloatArray {
            val unfilteredAccelerometerSample = FloatArray(3)
            System.arraycopy(event.values, 0, unfilteredAccelerometerSample, 0, 3)
            return unfilteredAccelerometerSample
        }

        fun insertIntoLocationList(event: Location) {
            _unfilteredLocationList.value?.add(
                SensorData2(
                    "GPS",
                    event.time,
                    event.longitude.toFloat(),
                    event.latitude.toFloat(),
                    event.altitude.toFloat()
                )
            )
        }

        fun filterDataList(rawSampleData: FloatArray): FloatArray {
            val alpha = 0.9F
            val isStarted = true

            val filteredDataPrevSample = FloatArray(3)
            val unfilteredDataSample = rawSampleData
            val filteredDataSample = FloatArray(3)

            if (_stepNumber.value == 0) {
                filteredDataPrevSample[0] = unfilteredDataSample[0]
                filteredDataPrevSample[1] = unfilteredDataSample[1]
                filteredDataPrevSample[2] = unfilteredDataSample[2]
            } else {
                filteredDataPrevSample[0] =
                    alpha * filteredDataPrevSample[0] + (1 - alpha) * unfilteredDataSample[0]
                filteredDataPrevSample[1] =
                    alpha * filteredDataPrevSample[1] + (1 - alpha) * unfilteredDataSample[1]
                filteredDataPrevSample[2] =
                    alpha * filteredDataPrevSample[2] + (1 - alpha) * unfilteredDataSample[2]
            }

            if (isStarted) {
                filteredDataSample[0] = filteredDataPrevSample[0]
                filteredDataSample[1] = filteredDataPrevSample[1]
                filteredDataSample[2] = filteredDataPrevSample[2]
            }

            _stepNumber.value = _stepNumber.value?.plus(1)

            return filteredDataSample
        }

        override fun onSensorChanged(event: SensorEvent?) {
            var kindOfSensor = ""
            if (event != null) {
                when (event.sensor) {
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) -> kindOfSensor = "Accelerometer"
                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) -> kindOfSensor = "GyroScope"
                }
                if (_isRecording.value == true) {
                    val rawSampleDataParameter = setUpUnfilteredDataSample(event)
                    val filteredSampleData = filterDataList(rawSampleDataParameter)
                    addToFilteredSensorList(kindOfSensor, event, filteredSampleData)
                    addToUnfilteredSensorList(kindOfSensor, event)
                }
            }
        }

        fun buildSensorStringBuilder(sensorList: MutableList<SensorData2>): StringBuilder {

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
            val sensorSamplingRate: Int
            if (PowerSavingMode.getPowerMode()) {
                sensorSamplingRate = SensorManager.SENSOR_DELAY_NORMAL
            } else {
                sensorSamplingRate = SensorManager.SENSOR_DELAY_FASTEST
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
