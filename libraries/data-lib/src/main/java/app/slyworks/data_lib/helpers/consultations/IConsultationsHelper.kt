package app.slyworks.data_lib.helpers.consultations

import app.slyworks.data_lib.model.models.ConsultationResponse
import app.slyworks.data_lib.model.view_entities.ConsultationRequestVModel
import app.slyworks.utils_lib.Outcome
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single


/**
 * Created by Joshua Sylvanus, 6:36 AM, 07-Oct-2023.
 */
interface IConsultationsHelper {
    fun listenForConsultationRequestsUpdates(): Observable<List<ConsultationRequestVModel>>
    fun listenForResponsesToSentConsultationRequests(): Observable<List<ConsultationRequestVModel>>
    fun sendResponseToConsultationRequest(response: ConsultationResponse): Single<Outcome>
    fun sendConsultationRequest(request: ConsultationRequestVModel): Single<Outcome>

}