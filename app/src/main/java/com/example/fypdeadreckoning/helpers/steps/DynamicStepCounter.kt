package com.example.fypdeadreckoning.helpers.steps

class DynamicStepCounter() {

    var stepCount: Int = 0
        private set
    var sensitivity: Double = 1.0
        private set
    private var upperThreshold = 10.8 // threshold tested by nisarg patel
    private var lowerThreshold = 8.8 // threshold tested by nisarg patel

    private var firstRun = true
    private var peakFound = false

    private var upperCount: Int
    private var lowerCount = 0
    private var sumUpperAcc: Double
    private var sumLowerAcc: Double

    private var sumAcc: Double
    private var avgAcc: Double
    private var runCount: Int

    init {
        upperCount = lowerCount
        sumLowerAcc = 0.0
        sumUpperAcc = sumLowerAcc

        avgAcc = 0.0
        sumAcc = avgAcc
        runCount = 0

        sensitivity = 0.5
    }

    // Set the default values for sensitivity
    constructor(sensitivity: Double) : this() {
        this.sensitivity = sensitivity
    }

    // Checks peaks in acceleration
    fun findStep(acc: Float): Boolean {
        setThresholdsContinuous(acc)

        // Finds a peak above the upperThreshold
        if (acc > upperThreshold) {
            if (!peakFound) {
                stepCount++
                peakFound = true
                return true
            }
        } // No longer within peak
        else if (acc < lowerThreshold) {
            if (peakFound) {
                peakFound = false
            }
        }

        return false
    }

    // Dynamically updates sensitivity
    fun setThresholdsContinuous(acc: Float) {
        runCount++

        if (firstRun) {
            upperThreshold = acc + sensitivity
            lowerThreshold = acc - sensitivity

            avgAcc = acc.toDouble()

            firstRun = false
            return
        }

        // Moving average equation
        avgAcc =
            ((avgAcc) * ((runCount.toDouble() - 1.0) / runCount.toDouble())) + (acc / runCount.toDouble())

        upperThreshold = avgAcc + sensitivity
        lowerThreshold = avgAcc - sensitivity
    }

    fun clearStepCount() {
        stepCount = 0
    }
}