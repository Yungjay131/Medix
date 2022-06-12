package com.slyworks.models.models


sealed class Result<T>{
    data class Success<T>(var data:T? = null): Result<T>()
    data class Failure<T>(var data:T? = null) : Result<T>()

    companion object{
        fun <T> success(data:T? = null): Result<T> { return Success<T>(data) }
        fun <T> failure(error:T? = null): Result<T> { return Failure<T>(error) }
    }
}



