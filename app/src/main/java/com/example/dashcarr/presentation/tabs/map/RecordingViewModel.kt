package com.example.dashcarr.presentation.tabs.map

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecordingViewModel : ViewModel() {

    private var _startTimeMillis = MutableStateFlow<Long>(0)



    private var _resumedElapsedTimeMillis = MutableStateFlow<Long>(0)


    private var _pauseDuration = MutableStateFlow<Long>(0)

    private var _pausedElapsedTimeMillis = MutableStateFlow<Long>(0)


    private var _elapsedTime = MutableStateFlow<String>("")
    val elapsedTime: StateFlow<String>
        get() = _elapsedTime

    var handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateElapsedTime()
            handler.postDelayed(this, 1000)
        }
    }

    fun startRecording() {
        viewModelScope.launch {
            _startTimeMillis.emit(SystemClock.elapsedRealtime())
        }
        handler.post(updateTimeRunnable)
    }

    fun pauseRecording() {
        viewModelScope.launch {
            _pausedElapsedTimeMillis.emit(SystemClock.elapsedRealtime())
        }
        handler.removeCallbacks(updateTimeRunnable)
    }

    fun resumeRecording() {
        viewModelScope.launch {
            val currentTime = SystemClock.elapsedRealtime()
            val timePaused = currentTime - _pausedElapsedTimeMillis.value
            _pauseDuration.value += timePaused

            _resumedElapsedTimeMillis.emit(currentTime)
        }

        handler.post(updateTimeRunnable)
    }

    fun stopRecording(){
        handler.removeCallbacks(updateTimeRunnable)
        // Reset all timing variables
        viewModelScope.launch {
            _startTimeMillis.emit(0)
            _pausedElapsedTimeMillis.emit(0)
            _pauseDuration.emit(0)
            _resumedElapsedTimeMillis.emit(0)
        }
    }


    fun updateElapsedTime() {

        val currentTimeMillis = SystemClock.elapsedRealtime()
        // Corrects for pausing
        val updatedTime = (currentTimeMillis - _startTimeMillis.value) - _pauseDuration.value

        val seconds = (updatedTime / 1000 % 60).toInt()
        val minutes = (updatedTime / (1000 * 60) % 60).toInt()
        val hours = (updatedTime / (1000 * 60 * 60) % 24).toInt()

        viewModelScope.launch {
            _elapsedTime.emit(String.format("%02d:%02d:%02d", hours, minutes, seconds))
        }
    }
}