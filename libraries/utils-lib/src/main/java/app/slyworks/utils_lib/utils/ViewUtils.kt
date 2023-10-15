package app.slyworks.utils_lib.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import androidx.annotation.ColorRes as ColorRes1


/**
 *Created by Joshua Sylvanus, 8:10 AM, 26/04/2022.
 */

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val TextView.properText: String
    get() = this.text.toString().trim()

val EditText.properText:String
    get() = this.text.toString().trim()

val EditText.properNumber: Int
    get() {
        if (this.text.toString().isBlank()) {
            return 0
        }
        return this.text.toString().toInt()
    }

val EditText.properPhoneNumber:String
    get() = this.text.toString()
        .trim()
        .replace("-", "")
        .replace(" ", "")

val EditText.properMoneyText:String
    get() = this.text.toString()
        .trim()
        .replace(",", "")


/**
 * show a Snackbar with duration set to #Snackbar.LENGTH_LONG */
fun Context.showToast(message:String):Unit =
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()


fun View.setChildViewsStatus(status:Boolean){
    isEnabled = status

    if(this is ViewGroup){
        val viewGroup: ViewGroup? = this as? ViewGroup

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
        .dontTransform()
        .into(this);
}

fun ImageView.displayImage(imageID: Uri) {
    Glide.with(this.context)
        .load(imageID)
        .dontTransform()
        .into(this);
}

fun ImageView.displayImage(image: Bitmap) {
    Glide.with(this.context)
        .load(image)
        .dontTransform()
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
    Glide.with(this.context)
        .asGif()
        .load(imageID)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this);
}

fun Activity.isInLandscape(): Boolean =
    (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)

fun Activity.setStatusBarVisibility(status: Boolean){
    /* remember this is cleared when user navigates away from the activity */
    if(status)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    else {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        findViewById<ViewGroup>(android.R.id.content).getChildAt(0).setFitsSystemWindows(true) /*.getchildAt(0)*/
    }
}

fun Activity.closeKeyboard(){
    val inputManager = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(currentFocus?.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
}

fun displayMessage(message:String, view:View):Unit =
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()

fun displayMessage(message:String, context:Context):Unit =
    Snackbar.make(
        (context as AppCompatActivity).findViewById(android.R.id.content),
        message, Snackbar.LENGTH_LONG).show()

fun Activity.makeFullScreen(){
    window.setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS )
}

fun TextView.setSpannableText(text:String,
                              start:Int,
                              end:Int,
                              shouldUnderline:Boolean = false,
                              @ColorRes1 colorResId:Int,
                              func:(() -> Unit)? = null){
    val spannableText: SpannableString = SpannableString(text)
    val clickableSpan: ClickableSpan =
        object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.setUnderlineText(shouldUnderline)
            }

            override fun onClick(p0: View) { func?.invoke() }
        }

    spannableText.setSpan(
        clickableSpan,
        start,
        end,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE )

    setLinkTextColor(ContextCompat.getColor(this.context, colorResId))
    setText(spannableText)
    setMovementMethod(LinkMovementMethod.getInstance())
    setHighlightColor(Color.TRANSPARENT)
}

fun EditText.setOnEditorAction(func:() -> Unit){
    setOnEditorActionListener { textView, i, keyEvent ->
        if(id == textView.id) {
            func()
            return@setOnEditorActionListener true
        }

        return@setOnEditorActionListener false
    }
}

fun EditText.setOnEditorAction(nextView:View){
    this.setOnEditorActionListener { textView, _id:Int?, _: KeyEvent ->
        Timber.e("${context as AppCompatActivity}")
        (context as AppCompatActivity).closeKeyboard()
        nextView.requestFocus()
        return@setOnEditorActionListener true
    }
}

fun View.togglePartialVisibility(status:Boolean? = null){
    if(status != null){
        if(status) visibility = View.VISIBLE
        else visibility = View.INVISIBLE

        return
    }

    when(visibility){
        View.VISIBLE -> visibility = View.INVISIBLE
        View.GONE -> visibility = View.VISIBLE
        View.INVISIBLE -> visibility = View.VISIBLE
    }
}

fun View.toggleVisibility(status:Boolean? = null){
    if(status != null){
        if(status) visibility = View.VISIBLE
        else visibility = View.GONE

        return
    }

    when(visibility){
        View.VISIBLE -> visibility = View.GONE
        View.GONE -> visibility = View.VISIBLE
        View.INVISIBLE -> visibility = View.VISIBLE
    }
}

fun toggleVisibility(vararg views:View) =
    views.toList().forEach { it.toggleVisibility() }

fun toggleVisibility(status: Boolean, vararg views:View) =
    views.toList().forEach { it.toggleVisibility(status) }




