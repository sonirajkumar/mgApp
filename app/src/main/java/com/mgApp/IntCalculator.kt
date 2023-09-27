package com.mgApp

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

class IntCalculator {
    @SuppressLint("SimpleDateFormat")
    fun calculateFinalAmount(tranListForIntCal: MutableList<MutableList<String>>, irForIntCal: String): Float{
        var pa = 0f
        for(i in 0 until tranListForIntCal.size){
            if(i != tranListForIntCal.size-1){
                if (tranListForIntCal[i][0]=="NAAME") {
                    pa += tranListForIntCal[i][1].toFloat()
                    val endDate = tranListForIntCal[i + 1][2]
                    val startDate = tranListForIntCal[i][2]
                    val tempEndDate = SimpleDateFormat("dd/MM/yyyy").parse(endDate)
                    val tempStartDate = SimpleDateFormat("dd/MM/yyyy").parse(startDate)
                    val dateDiffMs = (tempEndDate!!.time - tempStartDate!!.time).toFloat()
                    val dateDiffMonths: Float = ceil(dateDiffMs / (2635200000))
                    val dateDiffYears = floor(dateDiffMonths / 12)
                    val remainingMonths = ceil(dateDiffMonths % 12)

                    for (j in 1..dateDiffYears.toInt()) {
                        pa += calculateInterest(pa, irForIntCal.toFloat(), 12f)
                    }
                    pa += calculateInterest(pa, irForIntCal.toFloat(), remainingMonths)
                }
                else{
                    pa -= tranListForIntCal[i][1].toFloat()
                    val endDate = tranListForIntCal[i + 1][2]
                    val startDate = tranListForIntCal[i][2]
                    val tempEndDate = SimpleDateFormat("dd/MM/yyyy").parse(endDate)
                    val tempStartDate = SimpleDateFormat("dd/MM/yyyy").parse(startDate)
                    val dateDiffMs = (tempEndDate!!.time - tempStartDate!!.time).toFloat()
                    val dateDiffMonths: Float = ceil(dateDiffMs / (2592000000))
                    val dateDiffYears = floor(dateDiffMonths / 12)
                    val remainingMonths = ceil(dateDiffMonths % 12)

                    for (j in 1..dateDiffYears.toInt()) {
                        pa += calculateInterest(pa, irForIntCal.toFloat(), 12f)
                    }
                    pa += calculateInterest(pa, irForIntCal.toFloat(), remainingMonths)
                }
            }
            else{
                if (tranListForIntCal[i][0]=="NAAME") {
                    pa += tranListForIntCal[i][1].toFloat()
                    val startDate = tranListForIntCal[i][2]
                    val tempStartDate = SimpleDateFormat("dd/MM/yyyy").parse(startDate)
                    val dateDiffMs = (Date().time - tempStartDate!!.time).toFloat()
                    val dateDiffMonths: Float = ceil(dateDiffMs / (2592000000))
                    val dateDiffYears = floor(dateDiffMonths / 12)
                    val remainingMonths = ceil(dateDiffMonths % 12)

                    for (j in 1..dateDiffYears.toInt()) {
                        pa += calculateInterest(pa, irForIntCal.toFloat(), 12f)
                    }
                    pa += calculateInterest(pa, irForIntCal.toFloat(), remainingMonths)
                }
                else{
                    pa -= tranListForIntCal[i][1].toFloat()
                    val startDate = tranListForIntCal[i][2]
                    val tempStartDate = SimpleDateFormat("dd/MM/yyyy").parse(startDate)
                    val dateDiffMs = (Date().time - tempStartDate!!.time).toFloat()
                    val dateDiffMonths: Float = ceil(dateDiffMs / (2592000000))
                    val dateDiffYears = floor(dateDiffMonths / 12)
                    val remainingMonths = ceil(dateDiffMonths % 12)

                    for (j in 1..dateDiffYears.toInt()) {
                        pa += calculateInterest(pa, irForIntCal.toFloat(), 12f)
                    }
                    pa += calculateInterest(pa, irForIntCal.toFloat(), remainingMonths)
                }
            }

        }
        return pa
    }
    private fun calculateInterest(pa: Float, ir: Float, t: Float): Float {
        val tempAmount = pa * (1 + ((ir/100) * t))
        return tempAmount - pa
    }
}