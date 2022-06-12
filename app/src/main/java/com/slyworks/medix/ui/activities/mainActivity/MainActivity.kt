package com.slyworks.medix.ui.activities.mainActivity

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.collection.SimpleArrayMap
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.slyworks.constants.DOCTOR
import com.slyworks.constants.EVENT_UPDATE_MESSAGE_COUNT
import com.slyworks.constants.EXTRA_MAIN_FRAGMENT
import com.slyworks.constants.KEY_UNREAD_MESSAGE_COUNT
import com.slyworks.medix.AppController.clearAndRemove
import com.slyworks.medix.R
import com.slyworks.medix.Subscription
import com.slyworks.medix.UserDetailsUtils
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.medix.navigation.FragmentWrapper
import com.slyworks.medix.ui.custom_views.NetworkStatusView
import com.slyworks.medix.ui.dialogs.ExitDialog
import com.slyworks.medix.ui.dialogs.LogoutDialog
import com.slyworks.medix.utils.*
import com.slyworks.models.models.AccountType
import com.slyworks.models.models.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class MainActivity : BaseActivity(),  NavigationView.OnNavigationItemSelectedListener {
    //region Vars
    private lateinit var drawer: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var bnvMain_layout:ConstraintLayout
    private lateinit var bnvMain:BottomNavigationView

    private lateinit var rootView:CoordinatorLayout

    private var networkStatusView:NetworkStatusView? = null

    private var mFragmentMap:SimpleArrayMap<FragmentWrapper, Int> = SimpleArrayMap()

    private var mSelectedItem:Int = 0

    private  val mViewModel: MainActivityViewModel by viewModels()
    //endregion


    override fun onStart() {
        super.onStart()

        mViewModel.subscribeToNetwork().observe(this) {
            if(networkStatusView == null)
                networkStatusView = NetworkStatusView.from(rootView, false)

            networkStatusView!!.setVisibilityStatus(it)
        }
    }

    override fun onStop() {
        super.onStop()

        mViewModel.unsubscribeToNetwork()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initFragmentMap()
        initBottomNavMenu()
        initData()
        checkIntent()

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
                    }
                }
            })

        mViewModel
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
        rootView = findViewById(R.id.layout_app_bar_main)
        drawer = findViewById(R.id.drawer_main)
        navView = findViewById(R.id.nav_view_main)
        navView.setNavigationItemSelectedListener(this)
        bnvMain = findViewById(R.id.bnv_main)

    }

    private fun initBottomNavMenu(){
        if(UserDetailsUtils.user!!.accountType == AccountType.DOCTOR.toString()){
            bnvMain.menu.add(Menu.NONE, R.id.action_home,1, getString(R.string.action_home_text))
                .setIcon(AppCompatResources.getDrawable(this,R.drawable.ic_home))
            bnvMain.menu.add(Menu.NONE, R.id.action_chats, 2, getString(R.string.action_chats_text))
                .setIcon(AppCompatResources.getDrawable(this,R.drawable.ic_chat))
        }else{
            bnvMain.menu.add(Menu.NONE, R.id.action_home, 1, getString(R.string.action_home_text))
                .setIcon(AppCompatResources.getDrawable(this,R.drawable.ic_home))
            bnvMain.menu.add(Menu.NONE, R.id.action_connect, 2, getString(R.string.action_connect_text))
                .setIcon(AppCompatResources.getDrawable(this,R.drawable.ic_people))
            bnvMain.menu.add(Menu.NONE, R.id.action_chats, 3, getString(R.string.action_chats_text))
                .setIcon(AppCompatResources.getDrawable(this,R.drawable.ic_chat))
        }

        updateActiveItem(R.id.action_home)
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
        else transaction.add(R.id.fragment_container_main, f, "${f::class.simpleName}")

        transaction.commit()
    }

    fun MainActivity.toggleDrawerState(){
        if(drawer.isOpen)
            drawer.close()
        else
            drawer.open()
    }

    fun MainActivity.toggleBNVState(status:Boolean){
      bnvMain_layout.isVisible = status
    }

}