package com.example.fypdeadreckoning.helpers

import kotlin.math.sqrt

// Holds functions to be reused across app
object ExtraFunctions {

    // Calculate norm of 3 dimensional vectors
    fun calcNorm(vararg args: Float): Float {
        var sumSq = 0.0f // Declare as Float
        for (arg in args) {
            // Use simple multiplication for squaring, which is fastest
            sumSq += arg * arg
        }
        // Use the Float version of sqrt and the result is already a Float
        return sqrt(sumSq)
    }
}