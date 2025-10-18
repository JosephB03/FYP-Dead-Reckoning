package com.example.fypdeadreckoning.helpers.extra

import org.ejml.data.DMatrixRMaj
import kotlin.math.sqrt

// Holds functions to be reused across app
object ExtraFunctions {
    val IDENTITY_MATRIX: Array<FloatArray?> = arrayOf(
        floatArrayOf(1f, 0f, 0f),
        floatArrayOf(0f, 1f, 0f),
        floatArrayOf(0f, 0f, 1f)
    )

    fun nsToSec(time: Long): Float { return time / 1000000000.0f }

    fun factorial(num: Int): Int {
        var factorial = 1
        for (i in 1..num) {
            factorial *= i
        }
        return factorial
    }

    fun addMatrices(a: Array<FloatArray?>, b: Array<FloatArray?>): Array<FloatArray?> {
        val numRows = a.size
        val numColumns = a[0]!!.size

        val c = Array<FloatArray?>(numRows) { FloatArray(numColumns) }

        for (row in 0..<numRows) for (column in 0..<numColumns) c[row]!![column] =
            a[row]!![column] + b[row]!![column]

        //a[][] + b[][] = c[][]
        return c
    }

    fun multiplyMatrices(a: Array<FloatArray?>?, b: Array<FloatArray?>?): Array<FloatArray?> {
        val aNonNull = a!!
        val bNonNull = b!!

        val numRows: Int = aNonNull.size
        val numCols: Int = bNonNull[0]!!.size
        val numElements: Int = bNonNull.size

        val c = Array<FloatArray?>(numRows) { FloatArray(numCols) }

        for (row in 0..<numRows) {
            for (col in 0..<numCols) {
                for (element in 0..<numElements) {
                    // Assert non-nullability for array access
                    c[row]!![col] += aNonNull[row]!![element] * bNonNull[element]!![col]
                }
            }
        }
        // a[][] * b[][] = c
        return c
    }

    fun scaleMatrix(a: Array<FloatArray?>?, scalar: Float): Array<FloatArray?> {
        val numRows = a!!.size
        val numColumns = a[0]!!.size

        val b = Array<FloatArray?>(numRows) { FloatArray(numColumns) }

        for (row in 0..<numRows) for (column in 0..<numColumns) b[row]!![column] =
            a[row]!![column] * scalar

        //a[][] * c = b[][]
        return b
    }

    fun denseMatrixToArray(matrix: DMatrixRMaj): Array<FloatArray?> {
        val array: Array<FloatArray?>? =
            Array(matrix.getNumRows()) { FloatArray(matrix.getNumCols()) }
        for (row in 0..<matrix.getNumRows()) for (col in 0..<matrix.getNumCols()) array!![row]!![col] =
            matrix.get(row, col).toFloat()
        return array!!
    }

    fun vectorToMatrix(array: DoubleArray): Array<DoubleArray?> {
        return arrayOf(
            doubleArrayOf(array[0]),
            doubleArrayOf(array[1]),
            doubleArrayOf(array[2])
        )
    }

    fun floatVectorToDoubleVector(floatValues: FloatArray): DoubleArray {
        val doubleValues = DoubleArray(floatValues.size)
        for (i in floatValues.indices) doubleValues[i] = floatValues[i].toDouble()
        return doubleValues
    }

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

    //    public static float polarShiftMinusHalfPI(double heading) {
    //        if (heading < -Math.PI / 2.0 && heading > -Math.PI)
    //            heading += 3 * Math.PI / 2.0;
    //        else
    //            heading -= Math.PI / 2.0;
    //        return (float)heading;
    //    }
    fun radsToDegrees(rads: Double): Float {
        var degrees = if (rads < 0) (2.0 * Math.PI + rads) else rads
        degrees *= (180.0 / Math.PI)
        return degrees.toFloat()
    }

    fun polarAdd(initHeading: Float, deltaHeading: Float): Float {
        val currHeading = initHeading + deltaHeading

        //convert 0 < h < 2pi or -2pi < h < 0 to -pi/2 < h < pi/2
        return if (currHeading < -Math.PI) ((currHeading % Math.PI) + Math.PI).toFloat()
        else if (currHeading > Math.PI) ((currHeading % Math.PI) + -Math.PI).toFloat()
        else currHeading.toFloat()
    }

    fun calcCompassDirection(magneticDirection: Float, gyroDirection: Float): Float {
        //complimentary filter
        //convert -pi/2 < h < pi/2 to 0 < h < 2pi
        var magneticDirection: Float = magneticDirection
        var gyroDirection: Float = gyroDirection
        if (magneticDirection < 0) magneticDirection = (magneticDirection % (2.0 * Math.PI)).toFloat()
        if (gyroDirection < 0) gyroDirection = (gyroDirection % (2.0 * Math.PI)).toFloat()

        var compassDirection = 0.02 * magneticDirection + 0.98 * gyroDirection

        //convert 0 < h < 2pi to -pi/2 < h < pi/2
        if (compassDirection > Math.PI) compassDirection = (compassDirection % Math.PI) + -Math.PI

        return compassDirection.toFloat()
    }
}