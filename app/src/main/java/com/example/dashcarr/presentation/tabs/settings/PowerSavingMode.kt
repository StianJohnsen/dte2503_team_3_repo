package com.example.dashcarr.presentation.tabs.settings

/**
 * This object manages the power saving mode settings of the application.
 * It offers functionalities to set and get the application's power mode, the phone's power mode, and the initial battery capacity.
 * It uses an enum class `PowerState` with values `AUTO` and `ON` to represent the app's power-saving states.
 * The phone's power mode is represented as a boolean value.
 */
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