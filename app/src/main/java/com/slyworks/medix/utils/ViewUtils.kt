package com.slyworks.medix.utils

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.slyworks.constants.EVENT_GET_DISPLAY_VIEW
import com.slyworks.medix.App
import com.slyworks.medix.AppController
import com.slyworks.medix.R
import com.slyworks.models.models.Observer


/**
 *Created by Joshua Sylvanus, 11:08 AM, 1/8/2022.
 */
object ViewUtils {
    //region Vars

    private val RVColors:IntArray = intArrayOf(
        R.color.appRVBlue2,
        R.color.appRVYellow,
        R.color.appRVPink,
        R.color.appRVGreen,
      /*  R.color.appRVPurple,
        R.color.appRVOrange*/
    )

    private val RVColorPair:List<Pair<Int,Int>> = listOf(
        R.color.appRVBlue2 to R.color.appGrey,
        R.color.appRVYellow to R.color.appTextColor3,
        R.color.appRVPink to R.color.appTextColor3,
        R.color.appRVGreen to R.color.appTextColor3,
    )
    private val range: IntRange = 0 until RVColors.size
    //endregion

    fun getColorPair():Pair<Int,Int> = RVColorPair[range.random()]
    fun getColor():Int = RVColors[range.random()]


    fun View.setChildViewsStatus(status:Boolean){
        isEnabled = status

        if(this is ViewGroup){
            val viewGroup:ViewGroup? = this as? ViewGroup

            for(i in 0 until getChildCount()){
                //recursively searching the child view, if its a parent too
                val child:View = getChildAt(i)
                child.setChildViewsStatus(status)
            }
        }
    }
    fun ImageView.displayImage(imageID: Int) {
        Glide.with(this.context)
            .load(imageID)
            .dontTransform()
            .into(this);
    }

    fun ImageView.displayImage(imageID: String) {
        Glide.with(this.context)
            .load(imageID)
            .centerCrop()
            .into(this);
    }

    fun ImageView.displayImage(imageID: Uri) {
        Glide.with(this.context)
            .load(imageID)
            .into(this);
    }

    fun ImageView.displayGif(imageID: Int) {
        Glide.with(this.context)
            .asGif()
            .load(imageID)
            .dontTransform()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this);
    }

    fun ImageView.displayGif(imageID: String) {
        Glide.with(App.getContext())
            .asGif()
            .load(imageID)
            .dontTransform()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this);
    }

    fun Context.displaySnackBar(message: String) {
        displaySnackBar((this as Observer), message);
    }

    private fun displaySnackBar(observer:Observer, message: String) {
        Snackbar.make(
            AppController.pullData(EVENT_GET_DISPLAY_VIEW, observer),
            message,
            Snackbar.LENGTH_SHORT
        )
            .show()
    }
}