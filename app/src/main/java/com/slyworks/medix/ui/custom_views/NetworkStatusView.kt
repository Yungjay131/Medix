package com.slyworks.medix.ui.custom_views

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.slyworks.medix.R
import com.slyworks.medix.utils.ViewUtils.displayImage
import com.slyworks.medix.utils.px
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 *Created by Joshua Sylvanus, 8:38 AM, 26/04/2022.
 */
class NetworkStatusView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr:Int = 0): ConstraintLayout(context,attrs,defStyleAttr) {

    private lateinit var view:View
    private var ivStatus:CircleImageView
    private var tvStatus: TextView

    private val mColorOfflineIV_id:Int = R.color.appRed
    private val mColorOnlineIV_id:Int = R.color.appGreen
    private val mColorOffline:Int = ContextCompat.getColor(context, R.color.appBackground)
    private val mColorOfflineIV:Int = ContextCompat.getColor(context, R.color.appRed)
    private val mColorOnlineIV:Int = ContextCompat.getColor(context, R.color.appGreen)
    private val mColorOnline:Int = ContextCompat.getColor(context, R.color.appCardBlue)

    private var mHasBeenInitialized:Boolean = false

    init{
        /*view = LayoutInflater.from(context)
                       .inflate(R.layout.network_notifier, this, false) as ConstraintLayout*/

        view = inflate(context, R.layout.network_notifier, this)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                   ViewGroup.LayoutParams.MATCH_PARENT)

        ivStatus = findViewById(R.id.ivNetworkNotifier)
        tvStatus = findViewById(R.id.tvStatus_network_status)


        animateViewsIn()
    }

    private fun animateViewsIn(){
        viewTreeObserver.addOnPreDrawListener(
            object: ViewTreeObserver.OnPreDrawListener{
                override fun onPreDraw(): Boolean {
                    viewTreeObserver.removeOnPreDrawListener(this)

                    val animation: AnimatorSet = AnimatorInflater.loadAnimator(
                        context,R.animator.network_status_view_offline_animator) as AnimatorSet
                    animation.setTarget(view)
                    animation.start()

                    mHasBeenInitialized = true
                    return false
                }
            })
    }

    companion object{
        private fun checkForExistingView(v:ViewGroup):NetworkStatusView?{
            for(i in 0 until v.childCount){
                val child:View = v.getChildAt(i)
                val returned = _checkForExistingView(child)

                if(returned is NetworkStatusView)
                    return returned
            }

            return null
        }
        private fun _checkForExistingView(v:View): NetworkStatusView? {
            when (v) {
                is NetworkStatusView -> {
                    return v
                }
                is ViewGroup -> {
                    for (i in 0 until v.childCount) {
                        val child: View = v.getChildAt(i)

                        if (child is NetworkStatusView)
                            return child
                        else
                            _checkForExistingView(child)
                    }
                }

                else -> {
                    return null
                }
            }

            return null
        }



      /*init and draw on the given view??? and returns reference for futures*/
        fun from(parent: ViewGroup, usePadding:Boolean = true): NetworkStatusView {
          var view: View? = null

          /*was initially thinking t*/
        /*  val v = checkForExistingView(parent)
          if (v != null)
              return v*/

            when(parent){
                is ConstraintLayout,
                is MotionLayout ->{
                    /*view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.network_notifier, parent, false)*/

                    view = NetworkStatusView(parent.context)
                    view.setId(View.generateViewId())

                    val constraintSet:ConstraintSet = ConstraintSet()
                    constraintSet.clone(parent as ConstraintLayout)

             val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                                                                     ConstraintLayout.LayoutParams.WRAP_CONTENT)
                    view.layoutParams = layoutParams

                    parent.addView(view)

                    constraintSet.connect(view.id, ConstraintSet.START, parent.id, ConstraintSet.START)
                    constraintSet.connect(view.id, ConstraintSet.END, parent.id, ConstraintSet.END)
                    constraintSet.connect(view.id, ConstraintSet.BOTTOM, parent.id, ConstraintSet.BOTTOM)


                    constraintSet.constrainWidth(view.id, ConstraintSet.MATCH_CONSTRAINT)
                    constraintSet.constrainHeight(view.id, ConstraintSet.WRAP_CONTENT)

                    /*re-aligning the main container to give the space at the bottom*/
                    val main_container:View = parent.children.first()
                    main_container.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                               ViewGroup.LayoutParams.WRAP_CONTENT)

                    constraintSet.connect(main_container.id, ConstraintSet.START, parent.id, ConstraintSet.START)
                    constraintSet.connect(main_container.id, ConstraintSet.END, parent.id, ConstraintSet.END)
                    constraintSet.connect(main_container.id, ConstraintSet.TOP, parent.id, ConstraintSet.TOP)
                    constraintSet.connect(main_container.id, ConstraintSet.BOTTOM, view.id, ConstraintSet.TOP)

                    constraintSet.constrainHeight(main_container.id, ConstraintSet.MATCH_CONSTRAINT)

                    constraintSet.applyTo(parent)
                }
                is CoordinatorLayout ->{
                    view = NetworkStatusView(parent.context)
                    view.setId(View.generateViewId())

                    val layoutParams = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT,
                                                                      CoordinatorLayout.LayoutParams.WRAP_CONTENT)
                    layoutParams.gravity = Gravity.BOTTOM

                    view.layoutParams = layoutParams

                    parent.addView(view)

                    val main_container:View = parent.children.first()
                    main_container.layoutParams = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT,
                                                                                 CoordinatorLayout.LayoutParams.MATCH_PARENT)
                        .apply {
                            anchorId = view.id
                            anchorGravity = Gravity.TOP
                            if(usePadding)
                               bottomMargin = 25.px
                        }

                }
            }

          return view!! as NetworkStatusView
        }
    }

    fun setVisibilityStatus(status:Boolean){
        if(status)
            makeInvisible()
        else
            makeVisible()
    }

    /*TODO:revisit this animation methods later*/
    private fun makeVisible2(){
        findViewTreeLifecycleOwner()!!.lifecycleScope.launch{
            this@NetworkStatusView.visibility = View.VISIBLE
            view.setBackgroundColor(mColorOffline)
            ivStatus.setImageResource(mColorOfflineIV_id)
            tvStatus.text = "offline"

            if(!mHasBeenInitialized)
                return@launch

            delay(2000)

            val animation: AnimatorSet = AnimatorInflater.loadAnimator(
                context, R.animator.network_status_view_offline_animator
            ) as AnimatorSet
            animation.setTarget(this)
            animation.start()
        }

    }

    private fun makeInvisible2(){
        findViewTreeLifecycleOwner()!!.lifecycleScope.launch{
            view.setBackgroundColor(mColorOnline)
            ivStatus.setImageResource(mColorOnlineIV_id)
            tvStatus.text = "online"

            delay(2000)

            val anim = AnimationUtils.loadAnimation(context,R.anim.network_notifier_online)
            this@NetworkStatusView.startAnimation(anim)
            this@NetworkStatusView.visibility = View.GONE
        }

    }

    private fun makeVisible() {
        this@NetworkStatusView.visibility = View.VISIBLE
    }
    private fun makeInvisible() {
        this@NetworkStatusView.visibility = View.INVISIBLE
    }

}