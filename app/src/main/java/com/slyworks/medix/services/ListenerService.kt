package com.slyworks.medix.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.database.*
import com.slyworks.constants.APP_SERVICE_ID
import com.slyworks.constants.OUTGOING_MESSAGE
import com.slyworks.constants.REQUEST_PENDING
import com.slyworks.medix.App
import com.slyworks.medix.UserDetailsUtils
import com.slyworks.medix.UsersManager
import com.slyworks.medix.utils.*
import com.slyworks.medix.utils.ActivityUtils.isAppInForeground
import com.slyworks.models.models.ConsultationRequest
import com.slyworks.models.models.VideoCallRequest
import com.slyworks.models.room_models.FBUserDetails
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.atomic.AtomicReference


/**
 *Created by Joshua Sylvanus, 12:03 PM, 1/21/2022.
 */

class ListenerService : Service() {
    //region Vars
    private lateinit var mUser: FBUserDetails
    private var mAreListenersSet = false
    //endregion

    private val mConsultationRequestsChildEventListener:ChildEventListener = object: ChildEventListener{
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?){}
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val request: ConsultationRequest = snapshot.getValue(ConsultationRequest::class.java)!!
            if(request.status != REQUEST_PENDING) return

            val message:String = "${request.details.fullName} would like to have a consultation with you"
            NotificationHelper.createConsultationRequestNotification(
                request.details.firebaseUID,
                UserDetailsUtils.user!!.firebaseUID,
                message
            )

        }
    }

    private val mMessagesChildEventListener:ChildEventListener = object: ChildEventListener{
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?){}
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val lastMessage: com.slyworks.models.room_models.Message = snapshot.children.last().getValue(com.slyworks.models.room_models.Message::class.java)!!
            if(lastMessage.type == OUTGOING_MESSAGE) return

            NotificationHelper.createReceivedMessageNotification()
        }
    }

    private val mVideoCallsChildEventListener:ChildEventListener = object: ChildEventListener{
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?){}
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val request: VideoCallRequest = snapshot.getValue(VideoCallRequest::class.java)!!
            if(request.status != REQUEST_PENDING) return

            val userDetails: FBUserDetails = snapshot.child("details").getValue(FBUserDetails::class.java)!!
            NotificationHelper.createIncomingVideoCallNotification(userDetails)
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(APP_SERVICE_ID, NotificationHelper.createAppServiceNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(isAppInForeground()){
            stopSelf()
            mAreListenersSet = false
        }else {
            CoroutineScope(Dispatchers.IO).launch {
                mUser = getUserDetails()

                listenForConsultationRequests()
                listenForNewMessages()
                listenForVideoCallRequests()

                mAreListenersSet = true
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        //check if there is actually a listener
        if(!mAreListenersSet) return

        detachListenerForConsultationRequests()
        detachListenerForNewMessages()
        detachListenerForVideoCalls()

        /*enqueueing the task again*/
        App.initStartServiceWork()
    }

    private suspend fun getUserDetails(): FBUserDetails {
        val job:Deferred<FBUserDetails> = CoroutineScope(Dispatchers.IO).async {
            val userDetails:AtomicReference<FBUserDetails> = AtomicReference()
            val job2 = CoroutineScope(Dispatchers.IO).async async_inner@{
                UsersManager.getUserFromDataStore()
                    .collectLatest {
                         userDetails.set(it)
                         this@async_inner.cancel()
                    }
            }

            job2.await()
            return@async userDetails.get()
        }

        return job.await()
    }
    private fun listenForConsultationRequests(){
        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(mUser.firebaseUID)
            .child("requests")
            .child("from")
            .addChildEventListener(mConsultationRequestsChildEventListener)
    }

    private fun listenForNewMessages(){
        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(mUser.firebaseUID)
            .child("messages")
            .addChildEventListener(mMessagesChildEventListener)

    }

    private fun listenForVideoCallRequests(){
        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(mUser.firebaseUID)
            .child("video_call_requests")
            .addChildEventListener(mVideoCallsChildEventListener)
    }


    private fun detachListenerForConsultationRequests(){
        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(mUser.firebaseUID)
            .child("requests")
            .child("from")
            .removeEventListener(mConsultationRequestsChildEventListener)
    }

    private fun detachListenerForNewMessages(){
        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(mUser.firebaseUID)
            .child("messages")
            .removeEventListener(mMessagesChildEventListener)
    }

    private fun detachListenerForVideoCalls(){
        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(UserDetailsUtils.user!!.firebaseUID)
            .child("video_call_requests")
            .removeEventListener(mVideoCallsChildEventListener)
    }
}