package com.slyworks.medix.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 *Created by Joshua Sylvanus, 11:25 AM, 1/13/2022.
 */
object TimeUtils {
    //region Vars
    const val A_DAY = 10_000_000_000L
    const val MORE_THAN_YESTERDAY  = 20_000_000_000L
    //endregion

    fun checkIfDateIsSameDay(t1:String, t2:String):Boolean{
        val sdf:SimpleDateFormat = SimpleDateFormat("dd", Locale.getDefault())

        val dateA:Int = sdf.format(Date(t1.toLong())).toInt()
        val dateB:Int = sdf.format(Date(t2.toLong())).toInt()

        return (dateB - dateA == 0)
    }

    /*TODO:fix time parsing*/
    fun convertTimeToString(timeStamp:String):String{
        if(checkIfTimeStampIsYesterday(timeStamp))
            return "yesterday"

        val time: Date = Date(timeStamp.toLong())
        if(checkIfTimeStampIsToday(timeStamp)){
            val sdf: DateFormat = SimpleDateFormat("h:mm a",Locale.getDefault())
            val currentDate: String = sdf.format(time)
            return currentDate
        }

        val sdf: DateFormat = SimpleDateFormat("dd/MM/yyyy",Locale.getDefault())
        val currentDate: String = sdf.format(time)

        return currentDate;
    }

    private fun checkIfTimeStampIsYesterday(timeStamp:String):Boolean{
        val __currentDate:Date = Calendar.getInstance().time
        val sdf: DateFormat = SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault())
        val _currentDate:String = sdf.format(__currentDate)
        val currentDate:Long = _currentDate.toLong()

        val _timeStamp:Long = timeStamp.toLong()
        val condition_1 = currentDate - _timeStamp >= A_DAY
        val condition_2 = currentDate - _timeStamp < MORE_THAN_YESTERDAY
        if(condition_1 && condition_2)
            return true

        return false
    }

    private fun checkIfTimeStampIsToday(timeStamp: String):Boolean{
        val __currentDate:Date = Calendar.getInstance().time
        val sdf: DateFormat = SimpleDateFormat("ddMMyyyyHHmm",Locale.getDefault())
        val _currentDate:String = sdf.format(__currentDate)
        val currentDate:Long = _currentDate.toLong()

        val _timeStamp:Long = timeStamp.toLong()
        val condition_1 = currentDate - _timeStamp < A_DAY
        if(condition_1)
            return true

        return false
    }

    fun getCurrentTime():Long{
        return System.currentTimeMillis()
    }

    fun getCurrentDate():Long{
        val sdf: DateFormat = SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time).toLong()
    }
}
