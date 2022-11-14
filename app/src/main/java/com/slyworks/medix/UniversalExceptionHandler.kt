package com.slyworks.medix

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import app.slyworks.navigator.Navigator
import com.slyworks.medix.ui.activities.splash_activity.SplashActivity
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess


/**
 *Created by Joshua Sylvanus, 8:51 PM, 09/08/2022.
 */
class UniversalExceptionHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    //region Vars
    private val newLine = "\n"
    private val errorMessage = StringBuilder()
    private val softwareInfo = StringBuilder()
    private val dateInfo = StringBuilder()
    //endregion

    override fun uncaughtException(t: Thread, e: Throwable) {
        val stackTrace:StringWriter = StringWriter()
        e.printStackTrace(PrintWriter(stackTrace))

        errorMessage.append(stackTrace.toString())

        softwareInfo.append("SDK: ")
        softwareInfo.append(Build.VERSION.SDK_INT)
        softwareInfo.append(newLine)
        softwareInfo.append("Release: ")
        softwareInfo.append(Build.VERSION.RELEASE)
        softwareInfo.append(newLine)
        softwareInfo.append("Incremental: ")
        softwareInfo.append(Build.VERSION.INCREMENTAL)
        softwareInfo.append(newLine)

        //dateInfo.append(Calendar.getInstance().time)
        dateInfo.append(newLine)

        Timber.e("Error: $errorMessage")
        Timber.e("Software: $softwareInfo")
        Timber.e("Date: $dateInfo")

        /*Navigator.intentFor<SplashActivity>(context)
            .addExtra<String>("Error", errorMessage.toString())
            .addExtra<String>("Software", softwareInfo.toString())
            .addExtra<String>("Date", dateInfo.toString())
            .navigate()*/

     android.os.Process.killProcess(android.os.Process.myPid())
     exitProcess(2)
    }
}