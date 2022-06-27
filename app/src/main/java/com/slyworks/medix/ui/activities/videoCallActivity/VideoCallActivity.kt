package com.slyworks.medix.ui.activities.videoCallActivity

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.util.concurrent.ListenableFuture
import com.slyworks.constants.*
import com.slyworks.medix.managers.CallManager
import com.slyworks.medix.R
import com.slyworks.medix.utils.UserDetailsUtils
import com.slyworks.medix.managers.VibrationManager
import com.slyworks.medix.navigation.ActivityWrapper
import com.slyworks.medix.navigation.NavigationManager
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.medix.utils.*
import com.slyworks.medix.utils.ViewUtils.displayImage
import com.slyworks.models.models.VideoCallRequest
import com.slyworks.models.room_models.FBUserDetails
import de.hdodenhof.circleimageview.CircleImageView
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration

class VideoCallActivity : BaseActivity() {
    //region Vars
    private lateinit var rootView:ConstraintLayout
    private lateinit var flMainVideoContainer:FrameLayout
    private lateinit var flSmallVideoContainer:FrameLayout
    private lateinit var fabAcceptCall:FloatingActionButton
    private lateinit var fabDeclineCall:FloatingActionButton
    private lateinit var fabToggleMute:FloatingActionButton
    private lateinit var fabSwitchVideo:FloatingActionButton
    private lateinit var fabEndCall:FloatingActionButton
    private lateinit var ivProfile:CircleImageView
    private lateinit var tvProfileName:TextView
    private lateinit var pvMain:PreviewView

    private var mMuteStatus:Boolean = false
    private var mIsVideoSwitched:Boolean = false

    private lateinit var mUser: FBUserDetails
    private lateinit var mUserDetails: FBUserDetails

    private lateinit var mRtcEngine: RtcEngine
    
    private lateinit var mCameraProviderFuture:ListenableFuture<ProcessCameraProvider>
    //endregion

    companion object{
        private var mIsInForeground:Boolean = false
        fun getForegroundStatus():Boolean =  mIsInForeground
    }
    private val mRtcEventHandler: IRtcEngineEventHandler = object: IRtcEngineEventHandler(){
        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            runOnUiThread {
                _setViewsForVideoCallStarted()
                setupRemoteVideoFeed(uid)
            }
        }

        //user left channel
        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            runOnUiThread { onRemoteUserLeft() }
        }

        //user toggled camera
        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
            runOnUiThread { onRemoteUserVideoToggle(uid, state) }
        }
    }

    override fun onStart() {
        super.onStart()
        super.onStart(this)
        mIsInForeground = true
    }

    override fun onStop() {
        super.onStop()
        mIsInForeground = false
    }

    override fun onDestroy() {
        mRtcEngine.leaveChannel()
        RtcEngine.destroy()
        super.onDestroy(this)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        super.onCreate(this)
    }

    private fun initCameraPreview(){
       mCameraProviderFuture = ProcessCameraProvider.getInstance(this)

        /*verifying that the initialization succeeded*/
        mCameraProviderFuture.addListener(Runnable {
            val cameraProvider:ProcessCameraProvider = mCameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider){
        /*select a camera and bind the lifecycle and use cases*/
        /*Create a Preview.
          Specify the desired camera LensFacing option.
          Bind the selected camera and any use cases to the lifecycle.
          Connect the Preview to the PreviewView*/
        val preview:Preview = Preview.Builder().build()

        val cameraSelector:CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        preview.setSurfaceProvider(pvMain.getSurfaceProvider())

        var camera: Camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)

        //pvMain.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        // FIll_CENTER is the default tho
        pvMain.scaleType = PreviewView.ScaleType.FILL_CENTER
    }

    private fun initData(){
        //in the case of making the call or joining the call
     //if join call,shw callers picture and name
     //else show person you are calling picture
      //1-calling
       val extras:Bundle = intent.getBundleExtra(EXTRA_ACTIVITY)!!
       val type:String = extras.getString(EXTRA_VIDEO_CALL_TYPE)!!
       mUserDetails = intent.getParcelableExtra<FBUserDetails>(EXTRA_VIDEO_CALL_USER_DETAILS)!!
       if(type == VIDEO_CALL_OUTGOING){
           val request: VideoCallRequest =
               VideoCallRequest(UserDetailsUtils.user!!, REQUEST_PENDING)
           CallManager.processVideoCall(
               type = TYPE_REQUEST,
               firebaseUID = mUserDetails.firebaseUID,
               request = request)
           _setViewsForOutgoingVideoCall(mUserDetails)
           joinChannel()
       }else{
           _setViewsForIncomingVideoCall(mUserDetails)
       }
    }


    private fun initViews(){
        rootView = findViewById(R.id.rootView)
        flMainVideoContainer = findViewById(R.id.flMainVideoContainer)
        flSmallVideoContainer = findViewById(R.id.flSmallVideoContainer)

        ivProfile = findViewById(R.id.ivProfile_activity_video_call)
        tvProfileName = findViewById(R.id.tvName_activity_video_call)

        fabAcceptCall = findViewById(R.id.fabAcceptVideoCall_activity_video_call)
        fabDeclineCall = findViewById(R.id.fabDeclineVideoCall_activity_video_call)

        fabToggleMute = findViewById(R.id.fabMute_activity_video_call)
        fabEndCall = findViewById(R.id.fabEndCall_activity_video_call)
        fabSwitchVideo = findViewById(R.id.fabSwitchCamera_activity_video_call)

        fabAcceptCall.setOnClickListener {
            CallManager.processVideoCall(
                type = TYPE_RESPONSE,
                firebaseUID = mUserDetails.firebaseUID,
                status = REQUEST_ACCEPTED)

            joinChannel()
        }

        fabDeclineCall.setOnClickListener {
            CallManager.processVideoCall(
                type = TYPE_RESPONSE,
                firebaseUID = mUserDetails.firebaseUID,
                status = REQUEST_DECLINED)

            NavigationManager.onBackPressed(this,true, ActivityWrapper.MAIN)

            /*val intent:Intent = Intent(this, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()*/
        }

        fabEndCall.setOnClickListener {
            leaveChannel()

            CallManager.processVideoCall(
                type = TYPE_RESPONSE,
                firebaseUID = mUserDetails.firebaseUID,
                status = REQUEST_DECLINED)

           NavigationManager.onBackPressed(this, true, ActivityWrapper.MAIN)
        }

        fabToggleMute.setOnClickListener {
            mMuteStatus = !mMuteStatus
            toggleMuteStatus(mMuteStatus)
        }

        fabSwitchVideo.setOnClickListener {
            mIsVideoSwitched = !mIsVideoSwitched
            toggleVideoStatus(mIsVideoSwitched)
        }
    }

    private fun _setViewsForIncomingVideoCall(userDetails: FBUserDetails){
        /*TODO: add vibration here to simulate a real call, with camera preview*/
        ivProfile.displayImage(userDetails.imageUri)
        tvProfileName.text = userDetails.fullName

        flSmallVideoContainer.visibility = View.GONE
        fabToggleMute.visibility = View.GONE
        fabSwitchVideo.visibility = View.GONE
        fabAcceptCall.visibility = View.VISIBLE
        fabDeclineCall.visibility = View.VISIBLE
        fabEndCall.visibility = View.GONE

        ivProfile.visibility = View.VISIBLE

        VibrationManager.vibrate(type = INCOMING_CALL_NOTIFICATION)
    }

    private fun _setViewsForOutgoingVideoCall(userDetails: FBUserDetails){
        ivProfile.displayImage(userDetails.imageUri)
        tvProfileName.text = userDetails.fullName

        flSmallVideoContainer.visibility = View.GONE
        fabToggleMute.visibility = View.GONE
        fabSwitchVideo.visibility = View.GONE
        fabAcceptCall.visibility = View.GONE
        fabDeclineCall.visibility = View.GONE

        fabEndCall.visibility = View.VISIBLE
    }

    private fun _setViewsForVideoCallStarted(){
        flSmallVideoContainer.visibility = View.VISIBLE
        fabAcceptCall.visibility = View.GONE
        fabDeclineCall.visibility = View.GONE
        fabToggleMute.visibility = View.VISIBLE
        fabSwitchVideo.visibility = View.VISIBLE
        fabEndCall.visibility = View.VISIBLE
        pvMain.visibility = View.GONE

        tvProfileName.visibility = View.GONE
        ivProfile.visibility = View.GONE
    }

    private fun initAgoraEngine(){
        try{
            mRtcEngine = RtcEngine.create(baseContext, AGORA_APP_ID, mRtcEventHandler)
        }catch (e:Exception){
           throw RuntimeException("fatal error: RtcEngine required ${Log.getStackTraceString(e)}")
        }

        setupSession()
    }

    private fun setupSession(){
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
        mRtcEngine.enableVideo()

        mRtcEngine.setVideoEncoderConfiguration( VideoEncoderConfiguration(
            VideoEncoderConfiguration.VD_1280x720,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
            VideoEncoderConfiguration.STANDARD_BITRATE,
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT)
        )
    }

    private fun setupLocalVideoFeed(){
        //surface view renders the stream from the front camera
        val videoSurface:SurfaceView = RtcEngine.CreateRendererView(baseContext)
        videoSurface.setZOrderMediaOverlay(true)

        flSmallVideoContainer.addView(videoSurface)

        //0 here is the user agora UID,but its an Int
        mRtcEngine.setupLocalVideo(VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT,0 ))
    }

    private fun setupRemoteVideoFeed(UID:Int){
        val videoSurface:SurfaceView = RtcEngine.CreateRendererView(baseContext)
        videoSurface.setZOrderMediaOverlay(true)

        flMainVideoContainer.addView(videoSurface)

        mRtcEngine.setupRemoteVideo(VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, UID))
        mRtcEngine.setRemoteSubscribeFallbackOption(io.agora.rtc.Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY)
    }

    private fun joinChannel(){
        mRtcEngine.joinChannel(
            VIDEO_CHANNEL_1_TEMP_TOKEN,
            VIDEO_CALL_CHANNEL,
            "",
            IDUtils.generateNewVideoCallUserID())

        setupLocalVideoFeed()
    }

    private fun leaveChannel(){
        mRtcEngine.leaveChannel()
        removeVideo(flSmallVideoContainer)
        removeVideo(flMainVideoContainer)
        //assign appropriate visibility to other elements
    }
    private fun onRemoteUserLeft(){
       removeVideo(flMainVideoContainer)
    }
    private fun removeVideo(container:FrameLayout){
        container.removeAllViews()
    }
    private fun onRemoteUserVideoToggle(UID:Int, state:Int){
     /*   val videoSurface:SurfaceView = flMainVideoContainer.getChildAt(0) as SurfaceView
        videoSurface.visibility = if(state == 0) View.GONE else View.VISIBLE

        //add an icon to let other user know that remote video has been disabled
        if(state == 0){
            val ivNoCamera: ImageView = ImageView(this)
            ivNoCamera.setImageResource(R.drawable.ic_videocam_off)

            flMainVideoContainer.addView(ivNoCamera)
        }else{
            val ivNoCamera:ImageView? = flMainVideoContainer.getChildAt(1) as ImageView
            if(ivNoCamera != null )
                flMainVideoContainer.removeView(ivNoCamera)
        }*/
    }

    private fun toggleMuteStatus(status:Boolean){
        mRtcEngine.muteLocalAudioStream(status)
        if(status){
            fabToggleMute.isSelected = true
            fabToggleMute.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.appBlack_semi)))
        }else{
            fabToggleMute.isSelected = false
            fabToggleMute.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent)))
        }
    }

    private fun toggleVideoStatus(status:Boolean){
        mRtcEngine.muteLocalVideoStream(status)
        flSmallVideoContainer.isVisible = status

        val videoSurface:SurfaceView = flSmallVideoContainer.getChildAt(0) as SurfaceView
        with(videoSurface){
            setZOrderMediaOverlay(status)
            isVisible = status
        }

        if(status){
            fabSwitchVideo.isSelected = true
            fabSwitchVideo.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.appBlack_semi)))
        }else{
            fabSwitchVideo.isSelected = false
            fabSwitchVideo.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent)))
        }
    }


    override fun onBackPressed() {
        NavigationManager.onBackPressed(this, true)
    }
}