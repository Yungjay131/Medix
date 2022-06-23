package com.slyworks.models.models

import java.lang.UnsupportedOperationException

/**
 *Created by Joshua Sylvanus, 7:06 AM, 4/2/2022.
 */
//TODO:make Parcelable
class Outcome
private constructor(private val value:Any? = null) {
    //region Vars
    val isSuccess: Boolean
    get() = value is Success<*>

    val isFailure:Boolean
    get() = value is Failure<*>

    val isError:Boolean
    get() = value is Error
    //endregion

    fun <T> getTypedValue():T{
        when{
            isSuccess -> return (value as Success<T>).value as T
            isFailure -> return (value as Failure<T>).value as T
            else -> throw UnsupportedOperationException("op not supported for Outcome#isError()")
        }
    }
    fun getValue():Any?{
        when {
            isSuccess -> return (value as Success<*>).value
            isFailure -> return (value as Failure<*>).value
            isError -> return (value as Error).error
            else -> return null
        }
    }

    fun getAdditionalInfo():String?{
       return when (value) {
           is Success<*> ->(value as? Success<*>)?.additionalInfo
           is Failure<*> ->(value as? Failure<*>)?.additionalInfo
           else ->(value as? Error)?.message
       }
    }


    fun <T> getOrNull():T?{
        return when {
            isSuccess -> value as T
            isFailure -> null
            else -> null
        }
    }

    companion object{
        fun <T> SUCCESS(value: T? = null, additionalInfo:String? = null): Outcome {
            return Outcome(createSuccessClass(value, additionalInfo))
        }
        fun <T> FAILURE(value: T? = null, reason:String? = null): Outcome {
            return Outcome(createFailureClass(value, reason))
        }
        fun <T> ERROR(value: T? = null, error:Exception? = null) : Outcome {
            return Outcome(createErrorClass(error, error?.message))
        }

        private fun <T> createSuccessClass(value:T, additionalInfo: String?):Any{
            return Success(value, additionalInfo)
        }

        private fun <T> createFailureClass(value:T, additionalInfo: String?):Any{
            return Failure(value,additionalInfo)
        }

        private fun createErrorClass(error:Exception?, message:String?):Any{
            return Error(error,message)
        }
    }

    private data class Success<T>(val value:T?, val additionalInfo:String?)
    private data class Failure<T>(val value:T?, val additionalInfo:String?)
   /* @JvmInline
    private value class Failure(val value:String?)*/
    private data class Error(val error:Exception?, val message:String?)
}