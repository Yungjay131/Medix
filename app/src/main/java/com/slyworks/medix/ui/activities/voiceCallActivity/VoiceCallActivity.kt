package com.slyworks.medix.ui.activities.voiceCallActivity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.text.TextUtils
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.slyworks.constants.*
import com.slyworks.medix.managers.CallManager
import com.slyworks.medix.R
import com.slyworks.medix.utils.UserDetailsUtils
import com.slyworks.medix.managers.VibrationManager
import com.slyworks.medix.navigation.*
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.medix.ui.activities.videoCallActivity.VideoCallActivity
import com.slyworks.medix.ui.dialogs.SwitchToVideoCallDialog
import com.slyworks.medix.utils.*
import com.slyworks.models.models.VoiceCallRequest
import com.slyworks.models.room_models.FBUserDetails
import de.hdodenhof.circleimageview.CircleImageView
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.models.ChannelMediaOptions
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import kotlin.math.abs

class VoiceCallActivity : BaseActivity() {
    private var mLoudSpeaker: Boolean = false
    private var mMuteStatus: Boolean = false

    //region Vars
    private val TAG: String? = VoiceCallActivity::class.simpleName

    // Fill the App ID of your project generated on Agora Console.
    private val APP_ID = ""

    // Fill the channel name.
    private val CHANNEL = ""

    // Fill the temp token generated on Agora Console.
    private val TOKEN = ""

    private var mUID: Int = 0

    private lateinit var ivProfile: CircleImageView
    private lateinit var tvName: TextView
    private lateinit var tvCallTime: TextView
    private lateinit var fabAcceptCall: FloatingActionButton
    private lateinit var fabDeclineCall: FloatingActionButton
    private lateinit var fabEndCall: FloatingActionButton
    private lateinit var fabLoudSpeaker: FloatingActionButton
    private lateinit var fabSwitchToVideoCall: FloatingActionButton
    private lateinit var fabMuteMic: FloatingActionButton


    private var mRtcEngine: RtcEngine? = null
    private var mHandler:Handler? = Handler(Looper.getMainLooper())

    private lateinit var mUserDetails: FBUserDetails
    //endregion


    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onLeaveChannel(stats: RtcStats?) {
            super.onLeaveChannel(stats)
        }


        /**Occurs when the local user joins a specified channel.
         * The channel name assignment is based on channelName specified in the joinChannel method.
         * If the uid is not specified when joinChannel is called, the server automatically assigns a uid.
         * @param channel Channel name
         * @param uid User ID
         * @param elapsed Time elapsed (ms) from the user calling joinChannel until this callback is triggered*/
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            mUID = uid
            setViewsForCallStarted()
           /* mHandler!!.post(Runnable {
                speaker.setEnabled(true)
                mute.setEnabled(true)
                join.setEnabled(true)
            })*/
        }

        /**Since v2.9.0.
         * This callback indicates the state change of the remote audio stream.
         * PS: This callback does not work properly when the number of users (in the Communication profile) or
         *     broadcasters (in the Live-broadcast profile) in the channel exceeds 17.
         * @param uid ID of the user whose audio state changes.
         * @param state State of the remote audio
         *   REMOTE_AUDIO_STATE_STOPPED(0): The remote audio is in the default state, probably due
         *              to REMOTE_AUDIO_REASON_LOCAL_MUTED(3), REMOTE_AUDIO_REASON_REMOTE_MUTED(5),
         *              or REMOTE_AUDIO_REASON_REMOTE_OFFLINE(7).
         *   REMOTE_AUDIO_STATE_STARTING(1): The first remote audio packet is received.
         *   REMOTE_AUDIO_STATE_DECODING(2): The remote audio stream is decoded and plays normally,
         *              probably due to REMOTE_AUDIO_REASON_NETWORK_RECOVERY(2),
         *              REMOTE_AUDIO_REASON_LOCAL_UNMUTED(4) or REMOTE_AUDIO_REASON_REMOTE_UNMUTED(6).
         *   REMOTE_AUDIO_STATE_FROZEN(3): The remote audio is frozen, probably due to
         *              REMOTE_AUDIO_REASON_NETWORK_CONGESTION(1).
         *   REMOTE_AUDIO_STATE_FAILED(4): The remote audio fails to start, probably due to
         *              REMOTE_AUDIO_REASON_INTERNAL(0).
         * @param reason The reason of the remote audio state change.
         *   REMOTE_AUDIO_REASON_INTERNAL(0): Internal reasons.
         *   REMOTE_AUDIO_REASON_NETWORK_CONGESTION(1): Network congestion.
         *   REMOTE_AUDIO_REASON_NETWORK_RECOVERY(2): Network recovery.
         *   REMOTE_AUDIO_REASON_LOCAL_MUTED(3): The local user stops receiving the remote audio
         *               stream or disables the audio module.
         *   REMOTE_AUDIO_REASON_LOCAL_UNMUTED(4): The local user resumes receiving the remote audio
         *              stream or enables the audio module.
         *   REMOTE_AUDIO_REASON_REMOTE_MUTED(5): The remote user stops sending the audio stream or
         *               disables the audio module.
         *   REMOTE_AUDIO_REASON_REMOTE_UNMUTED(6): The remote user resumes sending the audio stream
         *              or enables the audio module.
         *   REMOTE_AUDIO_REASON_REMOTE_OFFLINE(7): The remote user leaves the channel.
         *   @param elapsed Time elapsed (ms) from the local user calling the joinChannel method
         *                  until the SDK triggers this callback.*/
        override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
        }

        /**Occurs when a remote user (Communication)/host (Live Broadcast) joins the channel.
         * @param uid ID of the user whose audio state changes.
         * @param elapsed Time delay (ms) from the local user calling joinChannel/setClientRole
         *                until this callback is triggered.*/
        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
        }

        /**Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         * @param uid ID of the user whose audio state changes.
         * @param reason Reason why the user goes offline:
         *   USER_OFFLINE_QUIT(0): The user left the current channel.
         *   USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data
         *              packet was received within a certain period of time. If a user quits the
         *               call and the message is not passed to the SDK (due to an unreliable channel),
         *               the SDK assumes the user dropped offline.
         *   USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from
         *               the host to the audience.*/
        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
        }

        override fun onActiveSpeaker(uid: Int) {
            super.onActiveSpeaker(uid)
        }
    }

    companion object{
        private var mIsInForeground: Boolean = false

        fun getForegroundStatus(): Boolean = mIsInForeground

    }

    override fun onStart() {
        super.onStart()
        super.onStart(this)

        mIsInForeground = true
    }

    override fun onStop() {
        super.onStop()
        super.onStop(this)

       mIsInForeground = false
    }

    override fun onDestroy() {
        if (mRtcEngine != null)
            mRtcEngine!!.leaveChannel()
        mHandler!!.post(RtcEngine::destroy)
        mHandler = null
        mRtcEngine = null
        super.onDestroy(this)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_call)

        super.onCreate(this)

        /*TODO:pass user FBUserDetails as Intent bundle*/
    }

    private fun initAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, AGORA_APP_ID, mRtcEventHandler)
        } catch (e: Exception) {
            throw RuntimeException("error occurred initialising RtcEngine")
        }
    }

    private fun initViews() {
        ivProfile = findViewById(R.id.ivProfile_activity_voice_call)
        tvName = findViewById(R.id.tvName_activity_voice_call)
        tvCallTime = findViewById(R.id.tvCallTime_activity_voice_call)
        fabDeclineCall = findViewById(R.id.fabDeclineCall_activity_voice_call)
        fabAcceptCall = findViewById(R.id.fabAcceptCall_activity_voice_call)
        fabEndCall = findViewById(R.id.fabEndCall_activity_voice_call)
        fabLoudSpeaker = findViewById(R.id.fabLoudSpeaker_activity_voice_call)
        fabSwitchToVideoCall = findViewById(R.id.fabSwitchToVideoCall_activity_voice_call)
        fabMuteMic = findViewById(R.id.fabMuteMic_activity_voice_call)

        fabAcceptCall.setOnClickListener {
            joinChannel()
        }

        fabDeclineCall.setOnClickListener {
            CallManager.processVoiceCall(
                type = TYPE_RESPONSE,
                firebaseUID = mUserDetails.firebaseUID,
                status = REQUEST_DECLINED)

            this.onBackPressedDispatcher.onBackPressed()
        }

        fabEndCall.setOnClickListener {
            leaveChannel()

            CallManager.processVoiceCall(
                type = TYPE_RESPONSE,
                firebaseUID = mUserDetails.firebaseUID,
                status = REQUEST_DECLINED)

            this.onBackPressedDispatcher.onBackPressed()
        }

        fabLoudSpeaker.setOnClickListener {
            mLoudSpeaker = !mLoudSpeaker
            toggleSpeakerHeadphoneStatus(mLoudSpeaker)
        }

        fabMuteMic.setOnClickListener {
            mMuteStatus = !mMuteStatus
            toggleAudioStatus(mMuteStatus)
        }

        fabSwitchToVideoCall.setOnClickListener {
            val o:PublishSubject<Boolean> = PublishSubject.create()

            SwitchToVideoCallDialog(o).show(supportFragmentManager, "")

            o.subscribe {
                if (it) {
                    leaveChannel()

                    Navigator.intentFor<VideoCallActivity>(this)
                        .addExtra<String>(EXTRA_VIDEO_CALL_TYPE, VIDEO_CALL_OUTGOING)
                        .addExtra<Parcelable>(EXTRA_VIDEO_CALL_USER_DETAILS, mUserDetails)
                        .previousIsTop()
                        .finishCaller()
                        .navigate()
                }
            }

        }
    }

    private fun initData(){
        val bundle:Bundle = intent.getBundleExtra(EXTRA_ACTIVITY)!!
        val type:String = bundle.getString(EXTRA_VOICE_CALL_TYPE)!!
        mUserDetails = bundle.getParcelable<FBUserDetails>(EXTRA_VOICE_CALL_USER_DETAILS)!!
        if(type == VOICE_CALL_OUTGOING){
            val request: VoiceCallRequest =
                VoiceCallRequest(UserDetailsUtils.user!!, REQUEST_PENDING)
            CallManager.sendVoiceCallRequestViaFCM(mUserDetails)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when{
                        it.isSuccess ->{
                            CallManager.processVoiceCall(TYPE_REQUEST,
                                                          mUserDetails.firebaseUID,
                                                          request = request)
                            setViewsForOutgoingVoiceCall(mUserDetails)
                        }
                        it.isError || it.isFailure ->{}
                    }
                }

        }else{
            setViewsForIncomingVoiceCall(mUserDetails)
        }
    }

    private fun setViewsForOutgoingVoiceCall(details: FBUserDetails){}
    private fun setViewsForIncomingVoiceCall(details: FBUserDetails){
        VibrationManager.vibrate(INCOMING_CALL_NOTIFICATION)
    }
    private fun setViewsForCallStarted(){
        VibrationManager.stopVibration()
    }

    private fun leaveChannel(){
        mRtcEngine!!.leaveChannel()
    }


    /**
     * @param channelID Specify the channel name that you want to join.
     *                  Users that input the same channel name join the same channel.
     */
    private fun joinChannel(channelID: String = "") {
        /** Sets the channel profile of the Agora RtcEngine.
        CHANNEL_PROFILE_COMMUNICATION(0): (Default) The Communication profile.
        Use this profile in one-on-one calls or group calls, where all users can talk freely.
        CHANNEL_PROFILE_LIVE_BROADCASTING(1): The Live-Broadcast profile. Users in a live-broadcast
        channel have a role as either broadcaster or audience. A broadcaster can both send and receive streams;
        an audience can only receive streams.*/
        mRtcEngine!!.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)

        /*TODO:check if this actually has to be here*/
        /**In the demo, the default is to enter as the anchor.*/
        //mRtcEngine!!.setClientRole(IRtcEngineEventHandler.ClientRole.CLIENT_ROLE_BROADCASTER)

        /**Please configure accessToken in the string_config file.
         * A temporary token generated in Console. A temporary token is valid for 24 hours. For details, see
         *      https://docs.agora.io/en/Agora%20Platform/token?platform=All%20Platforms#get-a-temporary-token
         * A token generated at the server. This applies to scenarios with high-security requirements. For details, see
         *      https://docs.agora.io/en/cloud-recording/token_server_java?platform=Java*/
        var accessToken: String? = ""
        if (TextUtils.equals(accessToken, "") || TextUtils.equals(accessToken, "<#YOUR ACCESS TOKEN#>")) {
            accessToken = null
        }

        mRtcEngine!!.enableAudioVolumeIndication(1000, 3, true)

        val option: ChannelMediaOptions = ChannelMediaOptions()
        option.autoSubscribeAudio = true
        option.autoSubscribeVideo = true


        val res: Int = mRtcEngine!!.joinChannel(
            VIDEO_CHANNEL_1_TEMP_TOKEN,
            VIDEO_CALL_CHANNEL,
            "",
            IDUtils.generateNewVideoCallUserID(),
            option
        )

        if (res != 0) {
            // Usually happens with invalid parameters
            // Error code description can be found at:
            // en: https://docs.agora.io/en/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html
            // cn: https://docs.agora.io/cn/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html
            Timber.e(RtcEngine.getErrorDescription(abs(res)))
            return
        }

        /*disable appropriate button*/
    }

    private fun toggleAudioStatus(status: Boolean) {
        /*TODO:set appropriate button state*/
        mRtcEngine!!.muteLocalAudioStream(status)
    }

    private fun toggleSpeakerHeadphoneStatus(status: Boolean) {
        /*TODO:set appropriate button state and toggle icon*/
        mRtcEngine!!.setEnableSpeakerphone(status)
    }
}