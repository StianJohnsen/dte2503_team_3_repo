package com.example.dashcarr.presentation.tabs.map

import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * TimerController is a class that controls the timing of the recording.
 * It is used to start, pause, resume, and stop the recording.
 * It also calculates the elapsed time.
 */
class TimerController {

    private var _startTimeMillis = MutableStateFlow<Long>(0)
    private var _resumedElapsedTimeMillis = MutableStateFlow<Long>(0)
    private var _pauseDuration = MutableStateFlow<Long>(0)
    private var _pausedElapsedTimeMillis = MutableStateFlow<Long>(0)

    private var _isRecording = MutableStateFlow<Boolean>(false)

    suspend fun startRecording() {
        _startTimeMillis.emit(SystemClock.elapsedRealtime())
        _isRecording.emit(true)
    }

    suspend fun pauseRecording() {
        if (_isRecording.value) {
            Log.d(this::class.simpleName, "The recording is paused")
            _pausedElapsedTimeMillis.emit(SystemClock.elapsedRealtime())
            _isRecording.emit(false)
        }
    }

    suspend fun resumeRecording() {
        if (!_isRecording.value) {
            val currentTime = SystemClock.elapsedRealtime()
            val timePaused = currentTime - _pausedElapsedTimeMillis.value
            _pauseDuration.value += timePaused
            _resumedElapsedTimeMillis.emit(currentTime)
            _isRecording.emit(true)
        }
    }

    suspend fun stopRecording() {
        _isRecording.emit(false)
        // Reset all timing variables
        _pausedElapsedTimeMillis.emit(0)
        _pauseDuration.emit(0)
        _resumedElapsedTimeMillis.emit(0)
    }

    fun getElapsedTime(): String {
        val currentTimeMillis = SystemClock.elapsedRealtime()
        // Corrects for pausing
        val updatedTime = (currentTimeMillis - _startTimeMillis.value) - _pauseDuration.value

        val seconds = (updatedTime / 1000 % 60).toInt()
        val minutes = (updatedTime / (1000 * 60) % 60).toInt()
        val hours = (updatedTime / (1000 * 60 * 60) % 24).toInt()

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)

    }

}
