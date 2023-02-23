package app.slyworks.auth_feature

import android.app.Activity
import android.net.Uri
import app.slyworks.auth_feature.registration.RegistrationActivityViewModel
import dev.joshuasylvanus.navigator.interfaces.FragmentContinuationStateful

import io.reactivex.rxjava3.core.Observable


/**
 * Created by Joshua Sylvanus, 12:41 AM, 20-Dec-2022.
 */
interface IRegViewModel {
   val viewModel:RegistrationActivityViewModel
   val navigator: FragmentContinuationStateful

   fun toggleProgressView(status:Boolean)
}