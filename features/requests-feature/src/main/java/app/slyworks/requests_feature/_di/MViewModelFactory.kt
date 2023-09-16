package app.slyworks.requests_feature._di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.slyworks.requests_feature.ViewRequestViewModel
import javax.inject.Provider

/**
 * Created by Joshua Sylvanus, 7:53 PM, 20/05/2023.
 */
class MViewModelFactory
constructor(private val viewModels:
     Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when{
            modelClass.isAssignableFrom(ViewRequestViewModel::class.java) ->
                viewModels[ViewRequestViewModel::class.java] as T

            else -> throw IllegalArgumentException("please add class to MViewModelFactory before trying to instantiate it")
        }
    }
}
