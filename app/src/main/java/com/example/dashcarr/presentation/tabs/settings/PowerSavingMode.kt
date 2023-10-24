package com.example.dashcarr.presentation.tabs.settings


object PowerSavingMode {
    enum class PowerState {
        AUTO, ON
    }

    private lateinit var appPowerMode: PowerState
    private var initialBatteryCapacity = -1
    private var phonePowerMode = false

    fun setAppPowerMode(mode: PowerState) {
        appPowerMode = mode
    }

    fun setPhonePowerMode(mode: Boolean) {
        phonePowerMode = mode
    }


    fun getPowerMode(): Boolean {
        if (appPowerMode.name == PowerState.ON.name) {
            return true
        } else {
            return phonePowerMode
        }
    }

    fun setInitialBatteryCapacity(capacity: Int) {
        initialBatteryCapacity = capacity
    }

    fun getInitialBatteryCapacity(): Int {
        if (initialBatteryCapacity == -1) {
            throw IllegalStateException("initialBatteryCapacity needs to be set first.")
        }
        return initialBatteryCapacity
    }
}