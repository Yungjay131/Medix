package com.slyworks.medix.utils

import android.content.res.Resources
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.slyworks.medix.App


/**
 *Created by Joshua Sylvanus, 8:10 AM, 26/04/2022.
 */

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun <E>MutableList<E>.isLastItem():Boolean{
    return this.size == 1
}

fun showToast(message:String){
 /*   Toast.makeText(, message, Toast.LENGTH_LONG).show()*/
}

/**
 * show a Snackbar with duration set to #Snackbar.LENGTH_LONG*/
fun showMessage(message:String, view: View){
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
}

