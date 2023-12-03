package com.example.dashcarr.presentation.tabs.map.data

/**
 * Represents a single reading from a sensor.
 * Contains information about the type of sensor, the timestamp of the reading,
 * and the x, y, z coordinates of the sensor data.
 *
 * @property typeOfSensor The type of sensor from which the data is read (e.g., accelerometer, gyroscope).
 * @property timestamp The time at which the sensor data was recorded, in milliseconds since epoch.
 * @property x The x-coordinate or value of the sensor reading.
 * @property y The y-coordinate or value of the sensor reading.
 * @property z The z-coordinate or value of the sensor reading.
 */
data class SensorData(
    val typeOfSensor: String,
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val z: Float
)
