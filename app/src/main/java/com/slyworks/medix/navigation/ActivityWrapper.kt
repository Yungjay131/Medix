package com.slyworks.medix.navigation

import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.SimpleArrayMap
import androidx.fragment.app.FragmentManager
import com.slyworks.medix.R
import com.slyworks.medix.ui.activities.*
import com.slyworks.medix.ui.activities.loginActivity.LoginActivity
import com.slyworks.medix.ui.activities.mainActivity.MainActivity
import com.slyworks.medix.ui.activities.messageActivity.MessageActivity
import com.slyworks.medix.ui.activities.onBoardingActivity.OnBoardingActivity
import com.slyworks.medix.ui.activities.registrationActivity.RegistrationActivity
import com.slyworks.medix.ui.activities.registrationActivity.RegistrationDoctorActivity
import com.slyworks.medix.ui.activities.registrationActivity.RegistrationPatientActivity
import com.slyworks.medix.ui.activities.videoCallActivity.VideoCallActivity
import com.slyworks.medix.ui.activities.requestsActivity.RequestsActivity
import com.slyworks.medix.ui.activities.settingsActivity.SettingsActivity
import com.slyworks.medix.ui.activities.voiceCallActivity.VoiceCallActivity
import com.slyworks.medix.utils.isLastItem


/**
 *Created by Joshua Sylvanus, 5:14 AM, 2/6/2022.
 */


const val NOT_SET = 0

sealed class ActivityWrapper {
    companion object {
        private var mMap: SimpleArrayMap<String, ActivityWrapper> = SimpleArrayMap<String, ActivityWrapper>().apply {
            put(SplashActivity::class.simpleName, ActivityWrapper.SPLASH)
            put(OnBoardingActivity::class.simpleName, ActivityWrapper.ONBOARDING)
            put(LoginActivity::class.simpleName, ActivityWrapper.LOGIN)
            put(RegistrationActivity::class.simpleName, ActivityWrapper.REG)
            put(RegistrationPatientActivity::class.simpleName, ActivityWrapper.REG_PATIENT)
            put(RegistrationDoctorActivity::class.simpleName, ActivityWrapper.REG_DOCTOR)
            put(MainActivity::class.simpleName, ActivityWrapper.MAIN)
            put(MessageActivity::class.simpleName, ActivityWrapper.MESSAGE)
            put(VideoCallActivity::class.simpleName, ActivityWrapper.VIDEO_CALL)
            put(VoiceCallActivity::class.simpleName, ActivityWrapper.SPLASH)
            put(RequestsActivity::class.simpleName, ActivityWrapper.VIEW_REQUEST)
            put(SettingsActivity::class.simpleName, ActivityWrapper.SETTINGS)
        }

        fun from(simpleName: String): ActivityWrapper {
            return mMap[simpleName] ?: throw IllegalArgumentException("ActivityWrapper with that name does not exist")
        }
    }

    abstract fun getInstance(): Class<out AppCompatActivity>
    abstract fun setFragmentManager(fragmentManager: FragmentManager?)
    abstract fun getFragmentManager(): FragmentManager
    abstract fun getFragmentContainerID(): Int
    abstract fun addFragment(fragment: FragmentWrapper)
    abstract fun getNextFragment(): FragmentWrapper?
    abstract fun isThereNextItem(): Boolean
    abstract fun setDialog(dialog: DialogWrapper?)
    abstract fun getDialog(): DialogWrapper?
    abstract fun setIsRunning(status:Boolean)
    abstract fun isRunning():Boolean


    object SPLASH : ActivityWrapper() {
        //region Vars
        private var mFragmentManager: FragmentManager? = null
        private var mFragmentList: MutableList<FragmentWrapper> = mutableListOf()
        private var mDialog: DialogWrapper? = null
        private var mIsRunning:Boolean = false
        //endregion

        override fun getInstance(): Class<out AppCompatActivity> {
            return SplashActivity::class.java
        }

        override fun setFragmentManager(fragmentManager: FragmentManager?) {
            mFragmentManager = fragmentManager
        }

        override fun getFragmentManager(): FragmentManager {
            return mFragmentManager!!
        }

        override fun getFragmentContainerID(): Int {
            return NOT_SET
        }

        override fun addFragment(fragment: FragmentWrapper) {
            mFragmentList.filter { it != fragment }
            mFragmentList.add(fragment)
        }

        override fun getNextFragment(): FragmentWrapper? {
            val f: FragmentWrapper? = mFragmentList.lastOrNull()
            if (f != null)
                mFragmentList.removeLast()

            return f
        }

        override fun isThereNextItem(): Boolean {
            return mFragmentList.isLastItem()
        }

        override fun setDialog(dialog: DialogWrapper?) {
            mDialog = dialog
        }

        override fun getDialog(): DialogWrapper? {
            return mDialog
        }

        override fun setIsRunning(status: Boolean) {
            mIsRunning = status
        }

        override fun isRunning(): Boolean {
            return mIsRunning
        }
    }

    object ONBOARDING : ActivityWrapper() {
        //region Vars
        private var mFragmentManager: FragmentManager? = null
        private val mFragmentList: MutableList<FragmentWrapper> = mutableListOf()
        private var mDialog: DialogWrapper? = null
        private var mIsRunning:Boolean = false
        //endregion

        override fun getInstance(): Class<out AppCompatActivity> {
            return OnBoardingActivity::class.java
        }

        override fun setFragmentManager(fragmentManager: FragmentManager?) {
            mFragmentManager = fragmentManager
        }

        override fun getFragmentManager(): FragmentManager {
            return mFragmentManager!!
        }

        override fun getFragmentContainerID(): Int {
            return NOT_SET
        }

        override fun addFragment(fragment: FragmentWrapper) {
            mFragmentList.filter { it != fragment }
            mFragmentList.add(fragment)
        }

        override fun getNextFragment(): FragmentWrapper? {
            val f: FragmentWrapper? = mFragmentList.lastOrNull()
            if (f != null)
                mFragmentList.removeLast()

            return f
        }

        override fun isThereNextItem(): Boolean {
            return mFragmentList.isLastItem()
        }

        override fun setDialog(dialog: DialogWrapper?) {
            mDialog = dialog
        }

        override fun getDialog(): DialogWrapper? {
            return mDialog
        }

        override fun setIsRunning(status: Boolean) {
            mIsRunning = status
        }

        override fun isRunning(): Boolean {
            return mIsRunning
        }
    }

    object LOGIN : ActivityWrapper() {
        //region Vars
        private var mFragmentManager: FragmentManager? = null
        private val mFragmentList: MutableList<FragmentWrapper> = mutableListOf()
        private var mDialog: DialogWrapper? = null
        private var mIsRunning:Boolean = false
        //endregion

        override fun getInstance(): Class<out AppCompatActivity> {
            return LoginActivity::class.java
        }

        override fun setFragmentManager(fragmentManager: FragmentManager?) {
            mFragmentManager = fragmentManager
        }

        override fun getFragmentManager(): FragmentManager {
            return mFragmentManager!!
        }

        override fun getFragmentContainerID(): Int {
            return NOT_SET
        }

        override fun addFragment(fragment: FragmentWrapper) {
            mFragmentList.filter { it != fragment }
            mFragmentList.add(fragment)
        }

        override fun getNextFragment(): FragmentWrapper? {
            val f: FragmentWrapper? = mFragmentList.lastOrNull()
            if (f != null)
                mFragmentList.removeLast()

            return f
        }

        override fun isThereNextItem(): Boolean {
            return mFragmentList.isLastItem()
        }

        override fun setDialog(dialog: DialogWrapper?) {
            mDialog = dialog
        }

        override fun getDialog(): DialogWrapper? {
            return mDialog
        }

        override fun setIsRunning(status: Boolean) {
            mIsRunning = status
        }

        override fun isRunning(): Boolean {
           return mIsRunning
        }
    }

    object REG : ActivityWrapper() {
        //region Vars
        private var mFragmentManager: FragmentManager? = null
        private val mFragmentsList: MutableList<FragmentWrapper> = mutableListOf()
        private var mDialog: DialogWrapper? = null
        private var mIsRunning:Boolean = false
        //endregion

        override fun getInstance(): Class<out AppCompatActivity> {
            return RegistrationActivity::class.java
        }

        override fun setFragmentManager(fragmentManager: FragmentManager?) {
            mFragmentManager = fragmentManager
        }

        override fun getFragmentManager(): FragmentManager {
            return mFragmentManager!!
        }

        override fun getFragmentContainerID(): Int {
            return NOT_SET
        }

        override fun addFragment(fragment: FragmentWrapper) {
            mFragmentsList.filter { it != fragment }
            mFragmentsList.add(fragment)
        }

        override fun getNextFragment(): FragmentWrapper? {
            val f: FragmentWrapper? = mFragmentsList.lastOrNull();
            if (f != null)
                mFragmentsList.removeLast()

            return f
        }

        override fun isThereNextItem(): Boolean {
            return mFragmentsList.isLastItem()
        }

        override fun setDialog(dialog: DialogWrapper?) {
            mDialog = dialog
        }

        override fun getDialog(): DialogWrapper? {
            return mDialog
        }

        override fun setIsRunning(status: Boolean) {
            mIsRunning = status
        }

        override fun isRunning(): Boolean {
            return mIsRunning
        }
    }

    object REG_PATIENT : ActivityWrapper() {
        //region Vars
        private var mFragmentManager: FragmentManager? = null
        private val mFragmentsList: MutableList<FragmentWrapper> = mutableListOf()
        private var mDialog: DialogWrapper? = null
        private var mIsRunning:Boolean = false
        //endregion

        override fun getInstance(): Class<out AppCompatActivity> {
            return RegistrationPatientActivity::class.java
        }

        override fun setFragmentManager(fragmentManager: FragmentManager?) {
            mFragmentManager = fragmentManager
        }

        override fun getFragmentManager(): FragmentManager {
            return mFragmentManager!!
        }

        override fun getFragmentContainerID(): Int {
            return NOT_SET
        }

        override fun addFragment(fragment: FragmentWrapper) {
            mFragmentsList.filter { it != fragment }
            mFragmentsList.add(fragment)
        }

        override fun getNextFragment(): FragmentWrapper? {
            val f: FragmentWrapper? = mFragmentsList.lastOrNull();
            if (f != null)
                mFragmentsList.removeLast()

            return f
        }

        override fun isThereNextItem(): Boolean {
            return mFragmentsList.isLastItem()
        }

        override fun setDialog(dialog: DialogWrapper?) {
            mDialog = dialog
        }

        override fun getDialog(): DialogWrapper? {
            return mDialog
        }

        override fun setIsRunning(status: Boolean) {
            mIsRunning = status
        }

        override fun isRunning(): Boolean {
            return mIsRunning
        }
    }

    object REG_DOCTOR : ActivityWrapper() {
        //region Vars
        private var mFragmentManager: FragmentManager? = null
        private val mFragmentList: MutableList<FragmentWrapper> = mutableListOf()
        private var mDialog: DialogWrapper? = null
        private var mIsRunning:Boolean = false
        //endregion

        override fun getInstance(): Class<out AppCompatActivity> {
            return RegistrationDoctorActivity::class.java
        }

        override fun setFragmentManager(fragmentManager: FragmentManager?) {
            mFragmentManager = fragmentManager
        }

        override fun getFragmentManager(): FragmentManager {
            return mFragmentManager!!
        }

        override fun getFragmentContainerID(): Int {
            return NOT_SET
        }

        override fun addFragment(fragment: FragmentWrapper) {
            mFragmentList.filter { it != fragment }
            mFragmentList.add(fragment)
        }

        override fun getNextFragment(): FragmentWrapper? {
            val f: FragmentWrapper? = mFragmentList.lastOrNull()
            if (f != null)
                mFragmentList.removeLast()

            return f
        }

        override fun isThereNextItem(): Boolean {
            return mFragmentList.isLastItem()
        }

        override fun setDialog(dialog: DialogWrapper?) {
            mDialog = dialog
        }

        override fun getDialog(): DialogWrapper? {
            return mDialog
        }

        override fun setIsRunning(status: Boolean) {
            mIsRunning = status
        }

        override fun isRunning(): Boolean {
            return mIsRunning
        }
    }

    object MAIN : ActivityWrapper() {
        //region Vars
        private var mFragmentManager: FragmentManager? = null
        private val mFragmentList: MutableList<FragmentWrapper> = mutableListOf()
        private var mDialog: DialogWrapper? = null
        private var mIsRunning:Boolean = false
        //endregion

        override fun getInstance(): Class<out AppCompatActivity> {
            return MainActivity::class.java
        }

        override fun setFragmentManager(fragmentManager: FragmentManager?) {
            mFragmentManager = fragmentManager
        }

        override fun getFragmentManager(): FragmentManager {
            return mFragmentManager!!
        }

        override fun getFragmentContainerID(): Int {
            return R.id.fragment_container_main
        }

        override fun addFragment(fragment: FragmentWrapper) {
            mFragmentList.filter { it != fragment }
            mFragmentList.add(fragment)
        }

        override fun getNextFragment(): FragmentWrapper? {
            val f: FragmentWrapper? = mFragmentList.lastOrNull()
            if (f != null)
                mFragmentList.removeLast()

            return f
        }

        override fun isThereNextItem(): Boolean {
            return mFragmentList.isLastItem()
        }

        override fun setDialog(dialog: DialogWrapper?) {
            mDialog = dialog
        }

        override fun getDialog(): DialogWrapper? {
            return mDialog
        }

        override fun setIsRunning(status: Boolean) {
            mIsRunning = status
        }

        override fun isRunning(): Boolean {
            return mIsRunning
        }
    }

    object MESSAGE : ActivityWrapper() {
        //region Vars
        private var mFragmentManager: FragmentManager? = null
        private val mFragmentList: MutableList<FragmentWrapper> = mutableListOf()
        private var mDialog: DialogWrapper? = null
        private var mIsRunning:Boolean = false
        //endregion

        override fun getInstance(): Class<out AppCompatActivity> {
            return MessageActivity::class.java
        }

        override fun setFragmentManager(fragmentManager: FragmentManager?) {
            mFragmentManager = fragmentManager
        }

        override fun getFragmentManager(): FragmentManager {
            return mFragmentManager!!
        }

        override fun getFragmentContainerID(): Int {
            return NOT_SET
        }

        override fun addFragment(fragment: FragmentWrapper) {
            mFragmentList.filter { it != fragment }
            mFragmentList.add(fragment)
        }

        override fun getNextFragment(): FragmentWrapper? {
            val f: FragmentWrapper? = mFragmentList.lastOrNull()
            if (f != null)
                mFragmentList.removeLast()

            return f
        }

        override fun isThereNextItem(): Boolean {
            return mFragmentList.isLastItem()
        }

        override fun setDialog(dialog: DialogWrapper?) {
            mDialog = dialog
        }

        override fun getDialog(): DialogWrapper? {
            return mDialog
        }

        override fun setIsRunning(status: Boolean) {
            mIsRunning = status
        }

        override fun isRunning(): Boolean {
            return mIsRunning
        }
    }

    object VIDEO_CALL : ActivityWrapper() {
        //region Vars
        private var mFragmentManager: FragmentManager? = null
        private var mFragmentList: MutableList<FragmentWrapper> = mutableListOf()
        private var mDialog: DialogWrapper? = null
        private var mIsRunning:Boolean = false
        //endregion

        override fun getInstance(): Class<out AppCompatActivity> {
            return VideoCallActivity::class.java
        }

        override fun setFragmentManager(fragmentManager: FragmentManager?) {
            mFragmentManager = fragmentManager
        }

        override fun getFragmentManager(): FragmentManager {
            return mFragmentManager!!
        }

        override fun getFragmentContainerID(): Int {
            return NOT_SET
        }

        override fun addFragment(fragment: FragmentWrapper) {
            mFragmentList.filter { it != fragment }
            mFragmentList.add(fragment)
        }

        override fun getNextFragment(): FragmentWrapper? {
            val f: FragmentWrapper? = mFragmentList.lastOrNull()
            if (f != null)
                mFragmentList.removeLast()

            return f
        }

        override fun isThereNextItem(): Boolean {
            return mFragmentList.isLastItem()
        }

        override fun setDialog(dialog: DialogWrapper?) {
            mDialog = dialog
        }

        override fun getDialog(): DialogWrapper? {
            return mDialog
        }

        override fun setIsRunning(status: Boolean) {
            mIsRunning = status
        }

        override fun isRunning(): Boolean {
            return mIsRunning
        }
    }

    object VOICE_CALL : ActivityWrapper() {
        //region Vars
        private var mFragmentManager: FragmentManager? = null
        private var mFragmentList: MutableList<FragmentWrapper> = mutableListOf()
        private var mDialog: DialogWrapper? = null
        private var mIsRunning:Boolean = false
        //endregion

        override fun getInstance(): Class<out AppCompatActivity> {
            return VoiceCallActivity::class.java
        }

        override fun setFragmentManager(fragmentManager: FragmentManager?) {
            mFragmentManager = fragmentManager
        }

        override fun getFragmentManager(): FragmentManager {
            return mFragmentManager!!
        }

        override fun getFragmentContainerID(): Int {
            return NOT_SET
        }

        override fun addFragment(fragment: FragmentWrapper) {
            mFragmentList.filter { it != fragment }
            mFragmentList.add(fragment)
        }

        override fun getNextFragment(): FragmentWrapper? {
            val f: FragmentWrapper? = mFragmentList.lastOrNull()
            if (f != null)
                mFragmentList.removeLast()

            return f
        }

        override fun isThereNextItem(): Boolean {
            return mFragmentList.isLastItem()
        }

        override fun setDialog(dialog: DialogWrapper?) {
            mDialog = dialog
        }

        override fun getDialog(): DialogWrapper? {
            return mDialog
        }

        override fun setIsRunning(status: Boolean) {
            mIsRunning = status
        }

        override fun isRunning(): Boolean {
            return mIsRunning
        }
    }

    object VIEW_REQUEST : ActivityWrapper() {
        //region Vars
        private var mFragmentManager: FragmentManager? = null
        private var mFragmentList: MutableList<FragmentWrapper> = mutableListOf()
        private var mDialog: DialogWrapper? = null
        private var mIsRunning:Boolean = false
        //endregion

        override fun getInstance(): Class<out AppCompatActivity> {
            return RequestsActivity::class.java
        }

        override fun setFragmentManager(fragmentManager: FragmentManager?) {
            mFragmentManager = fragmentManager
        }

        override fun getFragmentManager(): FragmentManager {
            return mFragmentManager!!
        }

        override fun getFragmentContainerID(): Int {
            return NOT_SET
        }

        override fun addFragment(fragment: FragmentWrapper) {
            mFragmentList.filter { it != fragment }
            mFragmentList.add(fragment)
        }

        override fun getNextFragment(): FragmentWrapper? {
            val f: FragmentWrapper? = mFragmentList.lastOrNull()
            if (f != null)
                mFragmentList.removeLast()

            return f
        }

        override fun isThereNextItem(): Boolean {
            return mFragmentList.isLastItem()
        }

        override fun setDialog(dialog: DialogWrapper?) {
            mDialog = dialog
        }

        override fun getDialog(): DialogWrapper? {
            return mDialog
        }

        override fun setIsRunning(status: Boolean) {
            mIsRunning = status
        }

        override fun isRunning(): Boolean {
            return mIsRunning
        }
    }

    object SETTINGS : ActivityWrapper() {
        //region Vars
        private var mFragmentManager: FragmentManager? = null
        private var mFragmentList: MutableList<FragmentWrapper> = mutableListOf()
        private var mDialog: DialogWrapper? = null
        private var mIsRunning:Boolean = false
        //endregion

        override fun getInstance(): Class<out AppCompatActivity> {
            return RequestsActivity::class.java
        }

        override fun setFragmentManager(fragmentManager: FragmentManager?) {
            mFragmentManager = fragmentManager
        }

        override fun getFragmentManager(): FragmentManager {
            return mFragmentManager!!
        }

        override fun getFragmentContainerID(): Int {
            return NOT_SET
        }

        override fun addFragment(fragment: FragmentWrapper) {
            mFragmentList.filter { it != fragment }
            mFragmentList.add(fragment)
        }

        override fun getNextFragment(): FragmentWrapper? {
            val f: FragmentWrapper? = mFragmentList.lastOrNull()
            if (f != null)
                mFragmentList.removeLast()

            return f
        }

        override fun isThereNextItem(): Boolean {
            return mFragmentList.isLastItem()
        }

        override fun setDialog(dialog: DialogWrapper?) {
            mDialog = dialog
        }

        override fun getDialog(): DialogWrapper? {
            return mDialog
        }

        override fun setIsRunning(status: Boolean) {
            mIsRunning = status
        }

        override fun isRunning(): Boolean {
            return mIsRunning
        }
    }
}