package com.example.dashcarr.presentation.tabs.map

import android.os.SystemClock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecordingViewModel {

    private var _startTimeMillis = MutableStateFlow<Long>(0)
    private var _resumedElapsedTimeMillis = MutableStateFlow<Long>(0)
    private var _pauseDuration = MutableStateFlow<Long>(0)
    private var _pausedElapsedTimeMillis = MutableStateFlow<Long>(0)
    private var _elapsedTime = MutableStateFlow<String>("")
    val elapsedTime = _elapsedTime.asStateFlow()

    private var isRecording = false

    suspend fun startRecording() {
        _startTimeMillis.emit(SystemClock.elapsedRealtime())
        isRecording = true
        updateElapsedTime()
    }

    suspend fun pauseRecording() {
        if (isRecording) {
            _pausedElapsedTimeMillis.emit(SystemClock.elapsedRealtime())
            isRecording = false
        }
    }

    suspend fun resumeRecording() {
        if (!isRecording) {
            val currentTime = SystemClock.elapsedRealtime()
            val timePaused = currentTime - _pausedElapsedTimeMillis.value
            _pauseDuration.value += timePaused
            _resumedElapsedTimeMillis.emit(currentTime)
            isRecording = true
            updateElapsedTime()
        }
    }

    suspend fun stopRecording() {
        isRecording = false
        // Reset all timing variables
        _startTimeMillis.emit(0)
        _pausedElapsedTimeMillis.emit(0)
        _pauseDuration.emit(0)
        _resumedElapsedTimeMillis.emit(0)
    }

    private suspend fun updateElapsedTime() {
        while (isRecording) {
            val currentTimeMillis = SystemClock.elapsedRealtime()
            // Corrects for pausing
            val updatedTime = (currentTimeMillis - _startTimeMillis.value) - _pauseDuration.value

            val seconds = (updatedTime / 1000 % 60).toInt()
            val minutes = (updatedTime / (1000 * 60) % 60).toInt()
            val hours = (updatedTime / (1000 * 60 * 60) % 24).toInt()

            _elapsedTime.emit(String.format("%02d:%02d:%02d", hours, minutes, seconds))

            delay(1000) // Delay for 1 second
        }
    }
}
