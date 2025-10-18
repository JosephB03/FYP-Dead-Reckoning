package com.example.fypdeadreckoning.helpers.orientation

import com.example.fypdeadreckoning.helpers.extra.ExtraFunctions
import kotlin.math.abs

class GyroscopeDeltaOrientation {
    private var isFirstRun = false
    private var sensitivity = 0f
    private var lastTimestamp = 0f
    private var gyroBias: FloatArray


    init {
        this.gyroBias = FloatArray(3)
        this.sensitivity = 0.0025f
        this.isFirstRun = true
    }

    constructor (sensitivity: Float, gyroBias: FloatArray?) {
        this.sensitivity = sensitivity
        if (gyroBias != null) {
            this.gyroBias = gyroBias
        }
    }

    fun calcDeltaOrientation(timestamp: Long, rawGyroValues: FloatArray): FloatArray? {
        //get the first timestamp
        if (isFirstRun) {
            isFirstRun = false
            lastTimestamp = ExtraFunctions.nsToSec(timestamp)
            return FloatArray(3)
        }

        val unbiasedGyroValues = removeBias(rawGyroValues)

        //return deltaOrientation[]
        return integrateValues(timestamp, unbiasedGyroValues)
    }

    fun setBias(gyroBias: FloatArray) {
        this.gyroBias = gyroBias
    }

    private fun removeBias(rawGyroValues: FloatArray): FloatArray {
        //ignoring the last 3 values of TYPE_UNCALIBRATED_GYROSCOPE, since the are only the Android-calculated biases
        val unbiasedGyroValues = FloatArray(3)

        for (i in 0..2) unbiasedGyroValues[i] = rawGyroValues[i] - gyroBias[i]

        //TODO: check how big of a difference this makes
        //applying a quick high pass filter
        for (i in 0..2) if (abs(unbiasedGyroValues[i]) > sensitivity) unbiasedGyroValues[i] =
            unbiasedGyroValues[i]
        else unbiasedGyroValues[i] = 0f

        return unbiasedGyroValues
    }

    private fun integrateValues(timestamp: Long, gyroValues: FloatArray): FloatArray {
        val currentTime: Float = ExtraFunctions.nsToSec(timestamp)
        val deltaTime = currentTime - lastTimestamp

        val deltaOrientation = FloatArray(3)

        //integrating angular velocity with respect to time
        for (i in 0..2) deltaOrientation[i] = gyroValues[i] * deltaTime

        lastTimestamp = currentTime

        return deltaOrientation
    }
}