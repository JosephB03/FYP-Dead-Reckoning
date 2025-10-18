package com.example.fypdeadreckoning.helpers.orientation

import com.example.fypdeadreckoning.helpers.extra.ExtraFunctions
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import org.ejml.simple.*
class MagneticFieldOrientation {

    val mRotationNED: SimpleMatrix = SimpleMatrix(
        arrayOf<DoubleArray?>(
            doubleArrayOf(0.0, 1.0, 0.0),
            doubleArrayOf(1.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, -1.0)
        )
    )

    fun getOrientationMatrix(gValues: FloatArray, mValues: FloatArray, mBias: FloatArray): Array<FloatArray?> {

        //remove bias from magnetic field initial values

        val mInitUnbiased: Array<DoubleArray?>? =
            ExtraFunctions.vectorToMatrix(removeBias(mValues, mBias))

        var mMInitUnbiased: SimpleMatrix? = SimpleMatrix(mInitUnbiased)
        var mGValues = SimpleMatrix(
            ExtraFunctions.vectorToMatrix(
                ExtraFunctions.floatVectorToDoubleVector(gValues)
            )
        )

        mMInitUnbiased = mRotationNED.mult(mMInitUnbiased)
        mGValues = mRotationNED.mult(mGValues)

        val gMValues: Array<FloatArray?> =
            ExtraFunctions.denseMatrixToArray(mGValues.getMatrix())

        //calculate roll and pitch from gravity
        val gR = atan2(gMValues[1]!![0].toDouble(), gMValues[2]!![0].toDouble())
        val gP = atan2(
            -gMValues[0]!![0].toDouble(),
            gMValues[1]!![0] * sin(gR) + gMValues[2]!![0] * cos(gR)
        )

        //create the rotation matrix representing the roll and pitch
        val rRP = arrayOf<DoubleArray?>(
            doubleArrayOf(cos(gP), sin(gP) * sin(gR), sin(gP) * cos(gR)),
            doubleArrayOf(0.0, cos(gR), -sin(gR)),
            doubleArrayOf(-sin(gP), cos(gP) * sin(gR), cos(gP) * cos(gR))
        )

        //convert arrays to matrices to allow for multiplication
        val mRRP = SimpleMatrix(rRP)

        //rotate magnetic field values in accordance to gravity readings
        val mMRP: SimpleMatrix = mRRP.mult(mMInitUnbiased)

        //calc heading (rads) from rotated magnetic field
        val h = -1 * (atan2(-mMRP.get(1), mMRP.get(0)) + 11.0 * Math.PI / 180.0)

        //rotation matrix representing heading, is negative when moving East of North
        val rH = arrayOf<DoubleArray?>(
            doubleArrayOf(cos(h), -sin(h), 0.0),
            doubleArrayOf(sin(h), cos(h), 0.0),
            doubleArrayOf(0.0, 0.0, 1.0)
        )

        //calc complete (initial) rotation matrix by multiplying roll/pitch matrix with heading matrix
        val mRH = SimpleMatrix(rH)
        val mR: SimpleMatrix = mRRP.mult(mRH)

        return ExtraFunctions.denseMatrixToArray(mR.getMatrix())
    }

    fun getDirection(gValues: FloatArray, mValues: FloatArray, mBias: FloatArray): Float {
        val orientationMatrix = getOrientationMatrix(gValues, mValues, mBias)
        return atan2(
            orientationMatrix[1]!![0].toDouble(),
            orientationMatrix[0]!![0].toDouble()
        ).toFloat()
    }


    private fun removeBias(mInit: FloatArray, mBias: FloatArray): DoubleArray {
        //ignoring the last 3 values of mInit, which are the android-calculated iron biases
        val mBiasRemoved = DoubleArray(3)
        for (i in 0..2) mBiasRemoved[i] = (mInit[i] - mBias[i]).toDouble()
        return mBiasRemoved
    }
}