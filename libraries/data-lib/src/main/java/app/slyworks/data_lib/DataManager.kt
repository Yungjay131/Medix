package app.slyworks.data_lib

import app.slyworks.constants_lib.*
import app.slyworks.data_lib.vmodels.*
import app.slyworks.room_lib.daos.CallHistoryDao
import app.slyworks.room_lib.daos.ConsultationRequestDao
import app.slyworks.room_lib.daos.MessageDao
import app.slyworks.room_lib.daos.PersonDao
import app.slyworks.room_lib.room_models.Message
import io.reactivex.rxjava3.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Created by Joshua Sylvanus, 8:21 PM, 25/11/2022.
 */

class DataManager(private val messageDao: MessageDao,
                  private val personDao: PersonDao,
                  private val callHistoryDao: CallHistoryDao,
                  private val consultationRequestDao: ConsultationRequestDao,
                  private val userDetailsUtils: UserDetailsUtils,
                  private val cryptoHelper: CryptoHelper
) {

    fun setUserDetails(userDetails: FBUserDetailsVModel) {
        userDetailsUtils.user = userDetails.reverseMap()
    }

    fun <T> getUserDetailsProperty(propertyKey:String = FBU_USER_DETAILS):T?{
        return when(propertyKey){
            FBU_USER_DETAILS -> userDetailsUtils.user?.transform() as? T?
            FBU_ACCOUNT_TYPE -> userDetailsUtils.user?.accountType as? T?
            FBU_FIRST_NAME -> userDetailsUtils.user?.firstName as? T?
            FBU_LAST_NAME -> userDetailsUtils.user?.lastName as? T?
            FBU_FULL_NAME -> userDetailsUtils.user?.fullName as? T?
            FBU_EMAIL -> userDetailsUtils.user?.email as? T?
            FBU_SEX -> userDetailsUtils.user?.sex as? T?
            FBU_AGE -> userDetailsUtils.user?.age as? T?
            FBU_FIREBASE_UID -> userDetailsUtils.user?.firebaseUID as? T?
            FBU_AGORA_UID -> userDetailsUtils.user?.agoraUID as? T?
            FBU_FCM_REGISTRATION_TOKEN -> userDetailsUtils.user?.FCMRegistrationToken as? T?
            FBU_IMAGE_URI -> userDetailsUtils.user?.imageUri as? T?
            FBU_HISTORY -> userDetailsUtils.user?.history as? T?
            FBU_SPECIALIZATION -> userDetailsUtils.user?.specialization as? T?
            else -> throw IllegalArgumentException("invalid propertyKey")
        }
    }

    fun observeUserDetailsFromDataStore(): Flowable<FBUserDetailsVModel> =
        Flowable.create(
            { emitter: FlowableEmitter<FBUserDetailsVModel> ->
                CoroutineScope(Dispatchers.Default).launch {
                    userDetailsUtils.getUserFromDataStore()
                        .collect {
                            userDetailsUtils.user = it
                            emitter.onNext(it.transform())
                        }
                }
            }, BackpressureStrategy.BUFFER)

    fun saveUserToDataStore(userDetails: FBUserDetailsVModel):Completable =
        Completable.create { emitter ->
            CoroutineScope(Dispatchers.Default).launch {
              userDetailsUtils.user = userDetails.reverseMap()
              userDetailsUtils.saveUserToDataStore(userDetails.reverseMap())
              emitter.onComplete()
            }
        }

    fun clearUserDetailsFromDataStore():Completable =
        Completable.create { emitter ->
            CoroutineScope(Dispatchers.Default).launch{
              userDetailsUtils.clearUserDetails()
              emitter.onComplete()
            }
        }

    private fun Array<out MessageVModel>.encrypt():Array<out MessageVModel>{
        forEach {
            val encrypted:String = cryptoHelper.encrypt(it.content)
            it.content = encrypted
        }
        return this
    }

    fun addMessages(vararg messages: MessageVModel):Unit =
        messageDao.addMessage(*messages.transform())

    fun updateMessages(vararg messages: MessageVModel):Int =
        messageDao.updateMessage(*messages.transform())

    fun deleteMessages(vararg messages: MessageVModel):Int =
        messageDao.deleteMessage(*messages.transform())

    fun observeMessages(): Flowable<MutableList<MessageVModel>> =
        messageDao.observeMessages().map(::mapMessage)

    fun observeMessagesForUID(firebaseUID:String): Flowable<MutableList<MessageVModel>> =
        messageDao.observeMessagesForUID(firebaseUID).map(::mapMessage)

    fun getMessages():MutableList<MessageVModel> =
        messageDao.getMessages().transformMessage()

    fun getMessageByID(messageID:String): MessageVModel =
        messageDao.getMessageByID(messageID).transform()

    fun getMessagesForUID(firebaseUID: String):MutableList<MessageVModel> =
        messageDao.getMessagesForUID(firebaseUID).transformMessage()

    fun getMessageCount():Long = messageDao.getMessageCount()

    fun getMessageCountForUID(firebaseUID: String):Long = messageDao.getMessageCountForUID(firebaseUID)

    fun getUnsentMessages():MutableList<MessageVModel> =
        messageDao.getUnsentMessages().transformMessage()

    fun addPersons(vararg persons: PersonVModel):Unit =
        personDao.addPersons(*persons.transform())

    fun updatePersons(vararg persons: PersonVModel):Int =
        personDao.updatePersons(*persons.transform())

    fun deletePersons(vararg persons: PersonVModel):Int =
        personDao.deletePersons(*persons.transform())

    fun getPersons():MutableList<PersonVModel> =
        personDao.getPersons().transformPerson()

    fun observePersons():Flowable<MutableList<PersonVModel>> =
        personDao.observePersons().map(::mapPerson)

    fun observePerson(firebaseUID: String):Flowable<PersonVModel> =
        personDao.observePerson(firebaseUID).map(::mapPerson)

    fun getPersonByID(firebaseUID: String): PersonVModel? =
        personDao.getPersonByID(firebaseUID)?.transform()

    fun getPersonCount():Long = personDao.getPersonCount()

    fun addCallHistory(vararg callHistory: CallHistoryVModel):Unit =
        callHistoryDao.addCallHistory(*callHistory.transform())

    fun updateCallHistory(vararg callHistory: CallHistoryVModel):Unit =
        callHistoryDao.updateCallHistory(*callHistory.transform())

    fun deleteCallHistory(vararg callHistory: CallHistoryVModel):Unit =
        callHistoryDao.deleteCallHistory(*callHistory.transform())

    fun observeCallHistory():Flowable<MutableList<CallHistoryVModel>> =
        callHistoryDao.observeCallHistory().map(::mapCallHistory)

    fun getCallHistory():MutableList<CallHistoryVModel> =
        callHistoryDao.getCallHistory().transformCallHistory()

    fun getCallHistoryCount():Long = callHistoryDao.getCallHistoryCount()

    fun addConsultationRequests(vararg requests: ConsultationRequestVModel):Unit =
        consultationRequestDao.addConsultationRequest(*requests.transform())

    fun updateConsultationRequests(vararg requests: ConsultationRequestVModel):Unit =
        consultationRequestDao.updateConsultationRequest(*requests.transform())

    fun deleteConsultationRequests(vararg requests: ConsultationRequestVModel):Unit =
        consultationRequestDao.deleteConsultationRequest(*requests.transform())

    fun observeConsultationRequests():Flowable<MutableList<ConsultationRequestVModel>> =
        consultationRequestDao.observeConsultationRequests().map(::mapConsultationRequest)

    fun getConsultationRequestsAsync(): Observable<MutableList<ConsultationRequestVModel>> =
        consultationRequestDao.getConsultationRequestsAsync().map(::mapConsultationRequest)

    fun getConsultationRequests():MutableList<ConsultationRequestVModel> =
        consultationRequestDao.getConsultationRequests().transformConsultationRequest()

    private fun Array<out MessageVModel>.transform():Array<out Message> =
        this.map(::reverseMapMessage).toTypedArray()

    private fun List<MessageVModel>.transform():Array<out Message> =
        this.map(::reverseMapMessage).toTypedArray()

    private fun MutableList<Message>.transformMessage():MutableList<MessageVModel> =
        this.map(::mapMessage) as MutableList<MessageVModel>

    private fun Message.transform(): MessageVModel = mapMessage(this)

    private fun mapMessage(messages:MutableList<Message>):MutableList<MessageVModel> =
        messages.map(::mapMessage) as MutableList<MessageVModel>

    /** decryption is done as the new MessageVModel object is being created */
    private fun mapMessage(message: Message): MessageVModel =
        MessageVModel(
            type = message.type,
            fromUID = message.fromUID,
            toUID = message.toUID,
            senderFullName = message.senderFullName,
            receiverFullName = message.receiverFullName,
            content = cryptoHelper.decrypt(message.content),
            timeStamp = message.timeStamp,
            messageID = message.messageID,
            status = message.status,
            senderImageUri = message.senderImageUri,
            accountType = message.accountType,
            FCMRegistrationToken = message.FCMRegistrationToken,
            receiverImageUri = message.receiverImageUri )

    /** encryption is done as the Message object is being created */
    private fun reverseMapMessage(message: MessageVModel): Message =
        Message(
            type = message.type,
            fromUID = message.fromUID,
            toUID = message.toUID,
            senderFullName = message.senderFullName,
            receiverFullName = message.receiverFullName,
            content = cryptoHelper.encrypt(message.content),
            timeStamp = message.timeStamp,
            messageID = message.messageID,
            status = message.status,
            senderImageUri = message.senderImageUri,
            accountType = message.accountType,
            FCMRegistrationToken = message.FCMRegistrationToken,
            receiverImageUri = message.receiverImageUri )

}