package com.slyworks.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


/**
 *Created by Joshua Sylvanus, 8:01 PM, 18/05/2022.
 */
const val KEY_PREFS_DEFAULT = "com.slyworks.utils.KEY_PREFS_DEFAULT"
const val DEFAULT_INT_VALUE = -1020304050
const val DEFAULT_FLOAT_VALUE = -0.1020304050F
const val DEFAULT_LONG_VALUE = 1020304050L

class PreferenceManager(private val context: Context) {
    //region Vars
     val mPrefs:SharedPreferences by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        context.getSharedPreferences(KEY_PREFS_DEFAULT, Context.MODE_PRIVATE)
        //PreferenceManager.getDefaultSharedPreferences(context)
     }
    private val editor:SharedPreferences.Editor = mPrefs.edit()
    //endregion

    fun clearPreference(vararg keys:String){
        keys.toList().forEach { editor.remove(it).apply() }
    }

   /* fun setWithDataStore(key: String, value:Any){

    }

    inline fun <reified E> getFromDataStore(key:String, default:E? = null):E?{

    }*/

    fun set(key:String, value:Any){
        CoroutineScope(Dispatchers.IO).launch{
            when(value){
                is Boolean -> editor.putBoolean(key, value).apply()
                is String -> editor.putString(key, value).apply()
                is Int -> editor.putInt(key, value).apply()
                is Float -> editor.putFloat(key, value).apply()
                is Long -> editor.putLong(key, value).apply()
                else -> throw IllegalArgumentException("type is not supported")
            }
        }
    }

    inline fun <reified T> get(key:String, defaultValue:T? = null):T?{
        var t:T? = null
        when(T::class){
            Boolean::class -> t = mPrefs.getBoolean(key, defaultValue as Boolean) as T
            String::class -> t = mPrefs.getString(key, defaultValue as? String) as? T?
            Int::class -> {
                val _t:Int = mPrefs.getInt(key, defaultValue as? Int ?: DEFAULT_INT_VALUE)
                if(_t != DEFAULT_INT_VALUE)
                   t = _t as T
            }
            Float::class ->{
                val _t:Float = mPrefs.getFloat(key, defaultValue as? Float ?: DEFAULT_FLOAT_VALUE)
                if(_t != DEFAULT_FLOAT_VALUE)
                    t = _t as T
            }
            Long::class ->{
                val _t:Long = mPrefs.getLong(key, defaultValue as? Long ?: DEFAULT_LONG_VALUE)
                if(_t != DEFAULT_LONG_VALUE)
                    t = _t as T
            }
            else -> throw UnsupportedOperationException()
        }

        return t
    }


}