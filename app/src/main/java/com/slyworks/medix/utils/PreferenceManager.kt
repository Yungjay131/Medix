package com.slyworks.medix.utils

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import androidx.preference.PreferenceManager
import com.slyworks.medix.App
import com.slyworks.medix.utils.PreferenceManager.edit


/**
 *Created by Joshua Sylvanus, 8:01 PM, 18/05/2022.
 */
object PreferenceManager {
    //region Vars
     val mPrefs:SharedPreferences by lazy(LazyThreadSafetyMode.NONE) {
        PreferenceManager.getDefaultSharedPreferences(App.getContext())
    }
    //endregion

    private fun SharedPreferences.edit(op:(SharedPreferences.Editor) -> Unit){
        val editor:SharedPreferences.Editor = this.edit()
        op(editor)
        editor.apply()
    }

    fun set(key:String, value:Any){
        with(mPrefs){
            when(value){
                is Editable -> {
                    if(value.toString() != ""){
                        edit { it.putString(key, value.toString()) }
                    }
                }
                is String -> {
                    if(value != ""){
                        edit { it.putString(key, value) }
                    }
                }
                is Int -> edit { it.putInt(key, value) }
                is Boolean -> edit { it.putBoolean(key, value) }
                is Float -> edit { it.putFloat(key, value) }
                is Long -> edit { it.putLong(key, value) }
                else -> throw UnsupportedOperationException("Unsupported Operation")
            }
        }
    }

    inline fun <reified T> get(key:String, defaultValue:T? = null):T{
        var t:T? = null
        with(mPrefs){
            t = when (T::class) {
                String::class -> getString(key, defaultValue as? String) as T?
                Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
                Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
                Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
                Long::class -> getLong(key, defaultValue as? Long ?: -1) as T?
                else -> throw UnsupportedOperationException("Unsupported Operation")
            }
        }

        return t!!
    }
}