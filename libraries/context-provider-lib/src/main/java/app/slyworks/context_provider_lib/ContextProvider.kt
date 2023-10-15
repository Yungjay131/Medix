package app.slyworks.context_provider_lib

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context


/**
 * Created by Joshua Sylvanus, 2:12 PM, 01-Jan-2023.
 */
@SuppressLint("StaticFieldLeak")
object ContextProvider {
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver

    fun setContext(ctx: Context){ context = ctx }
    fun getContext():Context = context

    fun getContentResolver(): ContentResolver = contentResolver
    fun setContentResolver(cr: ContentResolver){ contentResolver = cr }
}