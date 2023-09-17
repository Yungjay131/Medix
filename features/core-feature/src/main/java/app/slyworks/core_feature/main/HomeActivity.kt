package app.slyworks.core_feature.main

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.collection.SimpleArrayMap
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import app.slyworks.base_feature.ActivityUtils
import app.slyworks.base_feature.BaseActivity
import app.slyworks.base_feature.PermissionManager
import app.slyworks.base_feature.custom_views.setStatus
import app.slyworks.base_feature.ui.ExitDialog
import app.slyworks.base_feature.ui.LogoutDialog
import app.slyworks.core_feature.ProfileHostFragment
import app.slyworks.core_feature.R
import app.slyworks.core_feature.chat.ChatHostFragment
import app.slyworks.core_feature._di.ActivityComponent
import app.slyworks.core_feature.databinding.ActivityHomeBinding
import app.slyworks.core_feature.home.DoctorHomeFragment
import app.slyworks.core_feature.home.PatientHomeFragment
import app.slyworks.utils_lib.*
import app.slyworks.utils_lib.utils.plusAssign

import app.slyworks.utils_lib.utils.px
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import dev.joshuasylvanus.navigator.Navigator
import dev.joshuasylvanus.navigator.Navigator.Companion.getExtra
import dev.joshuasylvanus.navigator.interfaces.FragmentContinuationStateful
import io.reactivex.rxjava3.disposables.CompositeDisposable

import javax.inject.Inject

val Context.activityComponent: ActivityComponent
get() = (this as HomeActivity)._activityComponent

class HomeActivity : BaseActivity(),  NavigationView.OnNavigationItemSelectedListener {
    lateinit var _activityComponent: ActivityComponent

    private var fragmentMap:SimpleArrayMap<String, Int> = SimpleArrayMap()
    private var fragmentMap2:SimpleArrayMap<String, Int> = SimpleArrayMap()
    private lateinit var fragmentTag:String

    private lateinit var fragmentSupplier:() -> Fragment

    private var selectedItem:Int = -1

    private val disposables: CompositeDisposable = CompositeDisposable()

    private lateinit var bnvMain:BottomNavigationView

    private lateinit var navigator: FragmentContinuationStateful
    private lateinit var binding:ActivityHomeBinding

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var viewModel: HomeActivityViewModel

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()

        super.onCreate(savedInstanceState)

        /*binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFragmentMap()
        initData()
        initViews()*/

        initUsingAsyncLayoutInflater()
    }

    private fun initDI(){
        _activityComponent =
            ActivityComponent.getInitialBuilder()
                .build()
                .also{ it.inject(this@HomeActivity) }
    }

    private fun initUsingAsyncLayoutInflater():Unit =
        AsyncLayoutInflater(this)
            .inflate(R.layout.activity_home, findViewById<ViewGroup>(android.R.id.content))
            { view, resid, parent ->
                binding = ActivityHomeBinding.bind(view)
                setContentView(binding.root)

                initFragmentMap()
                initData()
                initViews()
            }


    private fun initFragmentMap(){
        if(viewModel.getUserAccountType() == DOCTOR){
            fragmentTag = FRAGMENT_DOCTOR_HOME

            fragmentSupplier = { DoctorHomeFragment.getInstance() }

            fragmentMap.put(FRAGMENT_DOCTOR_HOME, R.id.action_home)
            fragmentMap.put(FRAGMENT_CHAT_HOST, R.id.action_chats)

            fragmentMap2.put(DoctorHomeFragment::class.simpleName, R.id.action_home)
            fragmentMap2.put(ChatHostFragment::class.simpleName, R.id.action_chats)
        } else {
            fragmentTag = FRAGMENT_PATIENT_HOME

            fragmentSupplier = { PatientHomeFragment.getInstance() }

            fragmentMap.put(FRAGMENT_PATIENT_HOME, R.id.action_home)
            fragmentMap.put(FRAGMENT_CHAT_HOST, R.id.action_chats)
            fragmentMap.put(FRAGMENT_PROFILE_HOST, R.id.action_connect)

            fragmentMap2.put(PatientHomeFragment::class.simpleName, R.id.action_home)
            fragmentMap2.put(ChatHostFragment::class.simpleName, R.id.action_chats)
            fragmentMap2.put(ProfileHostFragment::class.simpleName, R.id.action_connect)
        }
    }



    private fun initData(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionManager.initialize(this, Manifest.permission.POST_NOTIFICATIONS)

            disposables +=
                permissionManager.getPermissionsObservable()
                    .subscribe({
                        when {
                            it.isSuccess -> {}
                            it.isFailure -> {}
                        }
                    }, {})
        }

        if(intent.hasExtra(EXTRA_MAIN_FRAGMENT))
            fragmentTag = intent.getExtra(EXTRA_MAIN_FRAGMENT)!!

        viewModel.subscribeToNetwork().observe(this){
            binding.mainLayout.networkStatusView.setStatus(it)
        }

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
                    }else
                        navigator.popBackStack{ bnvMain.setSelectedItemId(fragmentMap2[it]!!) }

                }
            })

    }
    
    private fun initViews(){
        val bnvLayout:View
        if(viewModel.getUserAccountType() == DOCTOR)
            bnvLayout  = LayoutInflater.from(this).inflate(R.layout.menu_doctor, binding.mainLayout.layoutAppBarMain, false)
        else
            bnvLayout = LayoutInflater.from(this).inflate(R.layout.menu_patient, binding.mainLayout.layoutAppBarMain, false)

        val constraintSet:ConstraintSet = ConstraintSet()
        constraintSet.clone(binding.mainLayout.layoutAppBarMain)

        bnvLayout.elevation = 4f
        binding.mainLayout.layoutAppBarMain.addView(bnvLayout)

        constraintSet.connect(bnvLayout.id, ConstraintSet.START, binding.mainLayout.layoutAppBarMain.id, ConstraintSet.START, (-16).px)
        constraintSet.connect(bnvLayout.id, ConstraintSet.END, binding.mainLayout.layoutAppBarMain.id, ConstraintSet.END, (-16).px)
        constraintSet.connect(bnvLayout.id, ConstraintSet.BOTTOM, binding.mainLayout.layoutAppBarMain.id, ConstraintSet.BOTTOM)

        constraintSet.constrainWidth(bnvLayout.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(bnvLayout.id, 80.px)

        constraintSet.applyTo(binding.mainLayout.layoutAppBarMain)

        bnvMain = findViewById(R.id.bnv_main)
        bnvMain.setSelectedItemId(fragmentMap[fragmentTag]!!)
        bnvMain.setOnItemSelectedListener(::handleBnvMenuItemClick)
        bnvMain.setOnItemReselectedListener(::handleBnvMenuItemClick)

        binding.navViewMain.setNavigationItemSelectedListener(this)

        _inflateFragment(fragmentSupplier.invoke())
    }

    private fun handleBnvMenuItemClick(item:MenuItem):Boolean {
        if (item.itemId == selectedItem)
            return true

        return when (item.itemId) {
            R.id.action_home -> {
                _inflateFragment(
                    if (viewModel.getUserAccountType() == DOCTOR)
                        DoctorHomeFragment.getInstance()
                    else
                        PatientHomeFragment.getInstance() )
                selectedItem = item.itemId
                true
            }
            R.id.action_chats -> {
                _inflateFragment(ChatHostFragment.getInstance())
                selectedItem = item.itemId
                true
            }
            R.id.action_connect -> {
                _inflateFragment(ProfileHostFragment.getInstance())
                selectedItem = item.itemId
                true
            }
            else -> throw UnsupportedOperationException()

        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        toggleDrawerState()

        when(item.getItemId()){
            R.id.action_logout -> {
                LogoutDialog(viewModel::logout).show(supportFragmentManager, "")
                return true
            }
            R.id.action_current_location -> {
                Navigator.intentFor(this, LOCATION_ACTIVITY_INTENT_FILTER)
                    .navigate()
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
    fun inflateFragment(f:String){
        //_inflateFragment(f)
        bnvMain.setSelectedItemId(fragmentMap[f]!!)
    }

    private fun _inflateFragment(f:Fragment){
        navigator
            .into(R.id.fragment_container_main)
            .show(f)
            .navigate()
    }

    fun verifyNotificationPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
          permissionManager.requestPermissions()
    }

    fun toggleProgressVisibility() {
        if(binding.progress.isVisible)
            binding.progress.isVisible = false
        else if(!binding.progress.isVisible)
            binding.progress.isVisible = true
    }

    fun toggleDrawerState():Unit =
        if(binding.drawerHome.isOpen) binding.drawerHome.close() else binding.drawerHome.open()
}