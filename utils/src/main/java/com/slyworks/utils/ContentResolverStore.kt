package com.slyworks.utils

import android.content.ContentResolver


/**
 *Created by Joshua Sylvanus, 9:26 PM, 12/07/2022.
 */
object ContentResolverStore {
    private var mContentResolver: ContentResolver? = null
    fun getContentResolver(): ContentResolver { return mContentResolver!! }
    fun setContentResolver(cr: ContentResolver?){ mContentResolver = cr!! }
    fun nullifyContentResolver(){ mContentResolver = null }
}