package com.slyworks.medix.ui.activities.main_activity

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.collection.SimpleArrayMap
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.slyworks.constants.*
import com.slyworks.medix.R
import com.slyworks.medix.appComponent
import com.slyworks.medix.di.components.ActivityComponent
import com.slyworks.medix.navigation.FragmentWrapper
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.medix.ui.custom_views.NetworkStatusView
import com.slyworks.medix.ui.dialogs.ExitDialog
import com.slyworks.medix.ui.dialogs.LogoutDialog
import com.slyworks.medix.ui.fragments.ProfileHostFragment
import com.slyworks.medix.ui.fragments.chatHostFragment.ChatHostFragment
import com.slyworks.medix.ui.fragments.homeFragment.DoctorHomeFragment
import com.slyworks.medix.ui.fragments.homeFragment.PatientHomeFragment
import com.slyworks.medix.utils.*
import com.slyworks.models.models.AccountType

.Companion.getParcelable
import javax.inject.Inject

val Context.activityComponent: ActivityComponent
get() = (this as MainActivity)._activityComponent

class MainActivity : BaseActivity(),  NavigationView.OnNavigationItemSelectedListener {
    //region Vars
    lateinit var _activityComponent: ActivityComponent

    private lateinit var drawer: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var bnvMain:BottomNavigationView

    private var rootView:CoordinatorLayout? = null

    private lateinit var progress:ProgressBar

    private var networkStatusView:NetworkStatusView? = null

    private var mFragmentMap:SimpleArrayMap<String, Int> = SimpleArrayMap()

    private var mSelectedItem:Int = -1

    @Inject
    lateinit var  mViewModel: MainActivityViewModel

    //private var mCurrentFragmentTag:String? = null
    //private var mFragmentTagList:MutableList<String> = mutableListOf()
    private lateinit var navigator: Navigator.FragmentContinuationStateful
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

    override fun onDestroy() {
        super.onDestroy()

        navigator.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()

        super.onCreate(savedInstanceState)

        initUsingAsyncLayoutInflater()
    }

    private fun initUsingAsyncLayoutInflater(){
        val content = findViewById<ViewGroup>(android.R.id.content)
        AsyncLayoutInflater(this)
            .inflate(R.layout.activity_main, content) { view, resid, parent ->
                setContentView(view)

                initData()
                initViews()
                initFragmentMap()
                initBottomNavMenu()
                initNetworkStatusView()
                initFragment()
            }
    }

    private fun initNetworkStatusView(){
        mViewModel.subscribeToNetwork().observe(this) {
            if(networkStatusView == null)
                networkStatusView = NetworkStatusView.from(rootView!!, MAIN)

            networkStatusView!!.setVisibilityStatus(it)
        }
    }
    private fun initFragmentMap(){
        if(mViewModel.getUserAccountType() == DOCTOR){
            mFragmentMap.put(DoctorHomeFragment::class.simpleName, R.id.action_home)
            mFragmentMap.put(ChatHostFragment::class.simpleName, R.id.action_chats)
        } else{
            mFragmentMap.put(PatientHomeFragment::class.simpleName, R.id.action_home)
            mFragmentMap.put(ChatHostFragment::class.simpleName, R.id.action_chats)
            mFragmentMap.put(ProfileHostFragment::class.simpleName, R.id.action_connect)
        }
    }
    private fun initDI(){
        _activityComponent = application.appComponent
            .activityComponentBuilder()
            .setActivity(this)
            .build()
        _activityComponent.inject(this)
    }

    private fun initData(){
       navigator = Navigator.transactionWithStateFrom(supportFragmentManager)

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
                        navigator.popBackStack{ updateActiveItem(mFragmentMap[it]!!)}
                    }
                }
            })

    }


    private fun initViews(){
        rootView = findViewById(R.id.layout_app_bar_main)
        drawer = findViewById(R.id.drawer_main)
        navView = findViewById(R.id.nav_view_main)
        navView.setNavigationItemSelectedListener(this)
    }

    private fun initBottomNavMenu(){
        val bnvLayout:View
        if(mViewModel.getUserAccountType() == AccountType.DOCTOR.toString())
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

        //updateActiveItem(R.id.action_home)
    }

    private fun handleBnvMenuItemClick(item:MenuItem):Boolean {
        if (item.itemId == mSelectedItem)
            return true

        return when (item.itemId) {
            R.id.action_home -> {
                _inflateFragment(
                    if (mViewModel.getUserAccountType() == DOCTOR)
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
        var fragment:FragmentWrapper = FragmentWrapper.HOME
        if(intent.hasExtra(EXTRA_MAIN_FRAGMENT))
            fragment = intent.getParcelable(EXTRA_MAIN_FRAGMENT)
        updateActiveItem(fragment)
    }

    fun updateActiveItem(f: FragmentWrapper) = updateActiveItem(mFragmentMap[f]!!)

    private fun updateActiveItem(@IdRes id:Int){
        bnvMain.setSelectedItemId(id)
        //mSelectedItem = id
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
        //_inflateFragment(f)
        updateActiveItem(mFragmentMap[f::class.simpleName]!!)
    }

    private fun _inflateFragment(f:Fragment){
        navigator
            .into(R.id.fragment_container_main)
            .show(f)
            .navigate()
    }

    fun toggleProgressBar(status:Boolean) { progress.isVisible = status }

    fun toggleDrawerState()= if(drawer.isOpen) drawer.close() else drawer.open()
}