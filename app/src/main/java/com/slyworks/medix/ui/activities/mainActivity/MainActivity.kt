package com.slyworks.medix.ui.activities.mainActivity

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.collection.SimpleArrayMap
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.slyworks.constants.*
import com.slyworks.medix.R
import com.slyworks.medix.UserDetailsUtils
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.medix.navigation.FragmentWrapper
import com.slyworks.medix.ui.custom_views.NetworkStatusView
import com.slyworks.medix.ui.dialogs.ExitDialog
import com.slyworks.medix.ui.dialogs.LogoutDialog
import com.slyworks.medix.ui.fragments.ProfileHostFragment
import com.slyworks.medix.ui.fragments.chatHostFragment.ChatHostFragment
import com.slyworks.medix.ui.fragments.findDoctorsFragment.FindDoctorsFragment
import com.slyworks.medix.ui.fragments.homeFragment.DoctorHomeFragment
import com.slyworks.medix.ui.fragments.homeFragment.PatientHomeFragment
import com.slyworks.medix.utils.*
import com.slyworks.models.models.AccountType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class MainActivity : BaseActivity(),  NavigationView.OnNavigationItemSelectedListener {
    //region Vars
    private lateinit var drawer: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var bnvMain_layout:ConstraintLayout
    private lateinit var bnvMain:BottomNavigationView

    private var rootView:CoordinatorLayout? = null

    private lateinit var progress:ProgressBar

    private var networkStatusView:NetworkStatusView? = null

    private var mFragmentMap:SimpleArrayMap<String, Int> = SimpleArrayMap()

    private var mSelectedItem:Int = R.id.action_home

    private  val mViewModel: MainActivityViewModel by viewModels()

    private var mCurrentFragmentTag:String? = null
    private var mFragmentTagList:MutableList<String> = mutableListOf()
    //endregion


    override fun onStart() {
        super.onStart()

        if(rootView == null)
            return

        initNetworkStatusView()
    }

    override fun onStop() {
        super.onStop()

        mViewModel.unsubscribeToNetwork()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initUsingAsyncLayoutInflater()
    }

    private fun initUsingAsyncLayoutInflater(){
        val content = findViewById<ViewGroup>(android.R.id.content)
        AsyncLayoutInflater(this)
            .inflate(R.layout.activity_main, content,
                object:AsyncLayoutInflater.OnInflateFinishedListener{
                    override fun onInflateFinished(view: View,
                                                   resid: Int,
                                                   parent: ViewGroup?) {
                        setContentView(view)

                        initViews()
                        initFragmentMap()
                        initBottomNavMenu()
                        initNetworkStatusView()
                        initFragment()
                        initData()
                    }
                })
    }

    private fun initNetworkStatusView(){
        mViewModel.subscribeToNetwork().observe(this) {
            if(networkStatusView == null)
                networkStatusView = NetworkStatusView.from(rootView!!, MAIN)

            networkStatusView!!.setVisibilityStatus(it)
        }
    }
    private fun initFragmentMap(){
        if(UserDetailsUtils.user!!.accountType == DOCTOR){
            mFragmentMap.put(DoctorHomeFragment::class.simpleName, R.id.action_home)
            mFragmentMap.put(ChatHostFragment::class.simpleName, R.id.action_chats)
        } else{
            mFragmentMap.put(PatientHomeFragment::class.simpleName, R.id.action_home)
            mFragmentMap.put(ChatHostFragment::class.simpleName, R.id.action_chats)
            mFragmentMap.put(FindDoctorsFragment::class.simpleName, R.id.action_connect)
        }
    }

    /*fixme:refactor to HomeFragment*/
    private fun clearOldUnreadMessageCount(){
        CoroutineScope(Dispatchers.IO).launch {
            PreferenceManager.set(KEY_UNREAD_MESSAGE_COUNT, 0)
        }
    }

    private fun initData(){
        this.onBackPressedDispatcher
            .addCallback(this,
            object:OnBackPressedCallback(true){
                override fun handleOnBackPressed() {
                    if(supportFragmentManager.backStackEntryCount == 1){
                        if(ActivityUtils.isLastActivity())
                            ExitDialog.getInstance()
                                .show(supportFragmentManager, "exit dialog")
                        else{
                            isEnabled = false
                            onBackPressed()
                            return
                        }
                    }else{
                        supportFragmentManager.popBackStack()
                        handleBackstackPop()
                    }
                }
            })

    }

    private fun handleBackstackPop(){
        mFragmentTagList.removeLast()
        mCurrentFragmentTag = mFragmentTagList.last()
        updateActiveItem(mFragmentMap[mCurrentFragmentTag!!]!!)
    }

    private fun initViews(){
        rootView = findViewById(R.id.layout_app_bar_main)
        drawer = findViewById(R.id.drawer_main)
        navView = findViewById(R.id.nav_view_main)
        navView.setNavigationItemSelectedListener(this)
    }

    private fun initBottomNavMenu(){
        val bnvLayout:View
        if(UserDetailsUtils.user!!.accountType == AccountType.DOCTOR.toString())
            bnvLayout  = LayoutInflater.from(this).inflate(R.layout.menu_doctor, rootView, false)
        else
            bnvLayout = LayoutInflater.from(this).inflate(R.layout.menu_patient,rootView, false)

        bnvLayout.layoutParams =  CoordinatorLayout.LayoutParams(
            CoordinatorLayout.LayoutParams.MATCH_PARENT,
            70.px)
            .apply {
                gravity = Gravity.BOTTOM
            }

        rootView!!.addView(bnvLayout)

        bnvMain = findViewById(R.id.bnv_main)
        bnvMain.setOnItemSelectedListener(::handleBnvMenuItemClick)
        bnvMain.setOnItemReselectedListener(::handleBnvMenuItemClick)

        updateActiveItem(R.id.action_home)
    }

    private fun handleBnvMenuItemClick(item:MenuItem):Boolean {
        if (item.itemId == mSelectedItem)
            return true

        return when (item.itemId) {
            R.id.action_home -> {
                _inflateFragment(
                    if (UserDetailsUtils.user!!.accountType == DOCTOR)
                        DoctorHomeFragment.getInstance()
                    else
                        PatientHomeFragment.getInstance()
                )
                mSelectedItem = item.itemId
                true
            }
            R.id.action_chats -> {
                _inflateFragment(ChatHostFragment.getInstance())
                mSelectedItem = item.itemId
                true
            }
            R.id.action_connect -> {
                _inflateFragment(ProfileHostFragment.getInstance())
                mSelectedItem = item.itemId
                true
            }
            else -> throw UnsupportedOperationException()

        }
    }

    private fun initFragment(){
        if(UserDetailsUtils.user!!.accountType == DOCTOR)
            _inflateFragment(DoctorHomeFragment.getInstance())
        else
            _inflateFragment(PatientHomeFragment.getInstance())
    }
    fun updateActiveItem(f:FragmentWrapper) = updateActiveItem(mFragmentMap[f]!!)

    private fun updateActiveItem(@IdRes id:Int){
        bnvMain.setSelectedItemId(id)
        mSelectedItem = id
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        toggleDrawerState()

        when(item.getItemId()){
            R.id.action_logout -> {
                LogoutDialog.getInstance()
                    .show(supportFragmentManager, "")
                return true
            }
            R.id.action_call_history ->{
                return true
            }
            R.id.action_requests ->{
                return true
            }
            R.id.action_settings ->{
                return true
            }
            R.id.action_support ->{
                return true
            }
            R.id.action_about ->{
                return true
            }
        }

        return true
    }

    /*called from outside this class*/
    fun inflateFragment(f:Fragment){
        _inflateFragment(f)
        updateActiveItem(mFragmentMap[f::class.simpleName]!!)
    }

    private fun _inflateFragment(f: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if(supportFragmentManager.findFragmentByTag(f::class.simpleName) != null){
            /*its been added before*/
            transaction.hide(supportFragmentManager.findFragmentByTag(mCurrentFragmentTag!!)!!)
            transaction.show(supportFragmentManager.findFragmentByTag(f::class.simpleName)!!)
        }
        else{
            if(mCurrentFragmentTag != null)
               transaction.hide(supportFragmentManager.findFragmentByTag(mCurrentFragmentTag!!)!!)

            transaction.addToBackStack("${f::class.simpleName}")
            transaction.add(R.id.fragment_container_main, f, "${f::class.simpleName}")
        }

        transaction.commit()
        mCurrentFragmentTag = f::class.simpleName
        mFragmentTagList = mFragmentTagList.filter{ it != f::class.simpleName} as MutableList<String>
        mFragmentTagList.add(mCurrentFragmentTag!!)
    }

    fun toggleProgressBar(status:Boolean) {
        progress.isVisible = status
    }

    fun toggleDrawerState(){
        if(drawer.isOpen)
            drawer.close()
        else
            drawer.open()
    }

    fun MainActivity.toggleBNVState(status:Boolean){
      bnvMain_layout.isVisible = status
    }

}