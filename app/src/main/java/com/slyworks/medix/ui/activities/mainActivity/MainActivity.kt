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

    private var mFragmentMap:SimpleArrayMap<FragmentWrapper, Int> = SimpleArrayMap()

    private var mSelectedItem:Int = R.id.action_home

    private  val mViewModel: MainActivityViewModel by viewModels()
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

    private fun initNetworkStatusView(){
        mViewModel.subscribeToNetwork().observe(this) {
            if(networkStatusView == null)
                networkStatusView = NetworkStatusView.from(rootView!!, MAIN)

            networkStatusView!!.setVisibilityStatus(it)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    initData()
                    checkIntent()
                    initNetworkStatusView()
                }
            })
    }

    private fun initFragmentMap(){
        if(UserDetailsUtils.user!!.accountType == DOCTOR){
            mFragmentMap.put(FragmentWrapper.HOME, R.id.action_home)
            mFragmentMap.put(FragmentWrapper.CHAT_HOST, R.id.action_chats)
        } else{
            mFragmentMap.put(FragmentWrapper.HOME, R.id.action_home)
            mFragmentMap.put(FragmentWrapper.FIND_DOCTORS, R.id.action_connect)
            mFragmentMap.put(FragmentWrapper.HOME, R.id.action_chats)
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
                    if(supportFragmentManager.backStackEntryCount == 0){
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
                    }
                }
            })

    }

    private fun checkIntent(){
        /*TODO: remove all the NavigationManager and FragmentWrapper related code - eventually*/
        var _f: FragmentWrapper? = null
        if(intent.getParcelableExtra<FragmentWrapper>(EXTRA_MAIN_FRAGMENT) != null){
            val f:FragmentWrapper = intent.getParcelableExtra<FragmentWrapper>(EXTRA_MAIN_FRAGMENT)!!
            _f = f
            updateActiveItem(mFragmentMap[_f]!!)
            inflateFragment(_f.getInstance())
            return
        }

        _f = FragmentWrapper.HOME
        updateActiveItem(mFragmentMap[_f]!!)
        inflateFragment(_f.getInstance())
    }


    private fun initViews(){
        progress = findViewById(R.id.progress_layout)
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
                inflateFragment(
                    if (UserDetailsUtils.user!!.accountType == DOCTOR)
                        DoctorHomeFragment.getInstance()
                    else
                        PatientHomeFragment.getInstance()
                )
                mSelectedItem = item.itemId
                true
            }
            R.id.action_chats -> {
                inflateFragment(ChatHostFragment.getInstance())
                mSelectedItem = item.itemId
                true
            }
            R.id.action_connect -> {
                inflateFragment(ProfileHostFragment.getInstance())
                mSelectedItem = item.itemId
                true
            }
            else -> throw UnsupportedOperationException()

        }
    }

    fun updateActiveItem(f:FragmentWrapper){
        updateActiveItem(mFragmentMap[f]!!)
    }

    private fun updateActiveItem(@IdRes id:Int){
        bnvMain.setSelectedItemId(id)
        mSelectedItem = id
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.getItemId()){
            R.id.action_logout -> {
                LogoutDialog.getInstance()
                    .show(supportFragmentManager, "")
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


    fun inflateFragment(f: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if(f.isAdded) transaction.show(f)
        else transaction.replace(R.id.fragment_container_main, f, "${f::class.simpleName}")

        transaction.commit()
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