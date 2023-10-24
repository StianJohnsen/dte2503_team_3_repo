package com.example.dashcarr.presentation.tabs.settings


object PowerSavingMode {
    enum class PowerState {
        AUTO, ON
    }

    private lateinit var appPowerMode: PowerState
    private var batteryPowerMode = false
    fun setAppPowerMode(mode: PowerState) {
        appPowerMode = mode
    }

    fun setBatteryMode(mode: Boolean) {
        batteryPowerMode = mode
    }


    fun getSaveBatteryMode(): Boolean {
        if (appPowerMode.name == PowerState.ON.name){
            return true
        }
        else {
            return batteryPowerMode
        }
    }
}