package com.iustinmoraru.prtracker.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.sqrt

/**
 * Detects a shake gesture with the accelerometer.
 * A shake is an acceleration above [thresholdG] times gravity; shakes closer than
 * [cooldownMs] are merged so one gesture triggers [onShake] only once.
 */
class ShakeDetector(
    context: Context,
    private val thresholdG: Float = 2.5f,
    private val cooldownMs: Long = 1_000L,
    private val onShake: () -> Unit,
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastShakeAt = 0L

    /** False on devices (or emulators) without an accelerometer. */
    val isAvailable: Boolean
        get() = accelerometer != null

    fun start() {
        accelerometer?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val (x, y, z) = Triple(event.values[0], event.values[1], event.values[2])
        val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
        val now = SystemClock.elapsedRealtime()
        if (gForce > thresholdG && now - lastShakeAt > cooldownMs) {
            lastShakeAt = now
            onShake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
