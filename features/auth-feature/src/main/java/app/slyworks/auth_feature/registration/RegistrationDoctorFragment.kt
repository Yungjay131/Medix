package app.slyworks.auth_feature.registration

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.KeyListener
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintSet
import app.slyworks.auth_feature.R
import app.slyworks.auth_feature.databinding.FragmentRegistrationDoctorBinding
import app.slyworks.base_feature.ui.TermsAndConditionsBSDialog
import app.slyworks.data_lib.model.models.VerificationDetails
import app.slyworks.utils_lib.LOGIN_ACTIVITY_INTENT_FILTER
import app.slyworks.utils_lib.utils.closeKeyboard

import app.slyworks.utils_lib.utils.displayMessage
import app.slyworks.utils_lib.utils.plusAssign
import app.slyworks.utils_lib.utils.px
import dev.joshuasylvanus.navigator.Navigator
import dev.joshuasylvanus.navigator.interfaces.FragmentContinuationStateful
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

import app.slyworks.base_feature.R as Base_R

class RegistrationDoctorFragment : Fragment() {
    //region Vars
    private var isThereCreatedLayout:Boolean = false

    @IdRes
    private val FALLBACK_ID:Int = R.id._view_horizontal_1
    @IdRes
    private var lastCreatedViewID:Int = FALLBACK_ID

    private var createdLayoutsList:MutableList<View> = mutableListOf()

    private val disposables = CompositeDisposable()

    private lateinit var navigator:FragmentContinuationStateful
    private lateinit var viewModel: RegistrationActivityViewModel

    private lateinit var binding: FragmentRegistrationDoctorBinding
    //endregion

    companion object {
        @JvmStatic
        fun newInstance(): RegistrationDoctorFragment = RegistrationDoctorFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRegistrationDoctorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initViews()
    }

    private fun initData(){
        navigator = (requireActivity() as RegistrationActivity).navigator
        viewModel = (requireActivity() as RegistrationActivity).viewModel

        viewModel.uiStateLD.observe(viewLifecycleOwner){
            when(it){
                is RegistrationUIState.RegistrationSuccess -> {
                    val dialog:SelectVerificationMethodBSDialog =
                        SelectVerificationMethodBSDialog.newInstance()

                    disposables +=
                        dialog.getSubject()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                dialog.dismiss()

                                when(it){
                                    VerificationDetails.EMAIL -> viewModel.verifyByEmail()
                                    VerificationDetails.OTP ->
                                        navigator.show(RegistrationOTP1Fragment.newInstance())
                                            .navigate()
                                }
                            }

                    dialog.show(requireActivity().supportFragmentManager,"")
                }

                is RegistrationUIState.EmailVerificationSuccess -> {
                    displayMessage("verification successful", binding.root)

                    /* delay 1 second then navigate back to LoginActivity */
                    Completable.timer(1_000, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Navigator.intentFor(requireContext(), LOGIN_ACTIVITY_INTENT_FILTER)
                                .newAndClearTask()
                                .navigate()
                        },
                            {
                                Timber.e(it)

                                Navigator.intentFor(requireContext(), LOGIN_ACTIVITY_INTENT_FILTER)
                                    .newAndClearTask()
                                    .navigate()
                            })
                }

                is RegistrationUIState.EmailVerificationFailure ->
                    displayMessage(it.error, binding.root)

                is RegistrationUIState.Message ->
                    displayMessage(it.message, binding.root)

                else -> {}
            }
        }

    }

    private fun initViews(){
        val spannableText: SpannableString = SpannableString("I have read and agree to the Terms and Conditions")
        val clickableSpan: ClickableSpan =
            object : ClickableSpan() {
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.setUnderlineText(true)
                }

                override fun onClick(p0: View) {
                    TermsAndConditionsBSDialog.getInstance()
                        .show(childFragmentManager, "")
                }
            }

        spannableText.setSpan(
            clickableSpan,
            spannableText.length - 21,
            spannableText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE )

        /* remember to set textColorLink*/
        binding.tvTandC.setText(spannableText)
        binding.tvTandC.setMovementMethod(LinkMovementMethod.getInstance())
        binding.tvTandC.setHighlightColor(Color.TRANSPARENT)

        binding.cbTandC.setOnCheckedChangeListener { _, status ->
            binding.btnSignUp.setEnabled(status)
        }

        binding.btnAddSpecialization.setOnClickListener{
            if (createdLayoutsList.isNotEmpty() ){
                val textView: EditText = createdLayoutsList.last()
                    .findViewById(Base_R.id.etText_layout_text)

                if(TextUtils.isEmpty(textView.text)){
                    displayMessage("please fill existing text fields before adding another", binding.root)
                    return@setOnClickListener
                }

                addLayout()
                return@setOnClickListener
            }

            //assuming no layout has been added before now
            addLayout()
        }

        binding.btnSignUp.setOnClickListener {
            requireActivity().closeKeyboard()

            if(!check())
                return@setOnClickListener

            viewModel.setSpecialization(parseUserSpecialization())
            viewModel.registerUser()
        }
    }

    private fun parseUserSpecialization():List<String>{
        val list = mutableListOf<CheckBox>(
            binding.cbGeneralHealth)

        val list2:List<String> =
            list.filter { it.isChecked }
                .map { it.text.toString() }

        val list3 = mutableListOf<String>()
        if(createdLayoutsList.isNotEmpty()){
            createdLayoutsList.forEach { layout ->
                list3.add(layout.findViewById<EditText>(Base_R.id.etText_layout_text).text.toString().trim())
            }
        }

        return mutableListOf<String>().plus(list2).plus(list3)
    }

    private fun check():Boolean {
        var status = true
        if(binding.cbTandC.isChecked().not()){
            displayMessage("you need to accept the Terms and Conditions to proceed", binding.root)
            status = false
        }else if(isThereCreatedLayout){
            createdLayoutsList.forEach { view ->
                val editText:EditText = view.findViewById(Base_R.id.etText_layout_text)
                if(TextUtils.isEmpty(editText.text)){
                    displayMessage("please fill in all added history texts or remove them", binding.root)
                    status = false
                }
            }
        }else if(parseUserSpecialization().isEmpty()){
            displayMessage("please check the \"general health\" checkbox or add at least 1 area of specialty", binding.root)
            status = false
        }

        return status
    }


    private fun addLayout() {
        val inflater: LayoutInflater = LayoutInflater.from(requireContext())

        /* inflate the custom editText view */
        val layout: View = inflater.inflate(app.slyworks.base_feature.R.layout.layout_text, binding.container, false)

        /* generate an ID that would be used to identify it later */
        layout.setId(View.generateViewId())

        /* get constrainSet for the container ConstraintLayout thats housing the
        * newly custom editText layout */
        val constraintSet: ConstraintSet = ConstraintSet()
        constraintSet.clone(binding.container)

        /* set the height of the custom EditText to 55dp??? */
        val layoutParams: ViewGroup.LayoutParams = layout.layoutParams
        layoutParams.height = 55

        /* set the click listener on the 'x' imageView of the custom EdtText */
        val ivCancel: ImageView = layout.findViewById(app.slyworks.base_feature.R.id.ivCancel)
        ivCancel.setOnClickListener {
            /* remove this created view */
            binding.container.removeView(layout)
            createdLayoutsList.remove(layout)

            if(createdLayoutsList.isEmpty())
                isThereCreatedLayout = false

            /* setting the anchor view for next created layout */
            lastCreatedViewID = createdLayoutsList.lastOrNull()?.id ?: FALLBACK_ID

            /* realign the add button to be below this added layout */
            val constraintSet: ConstraintSet = ConstraintSet()
            constraintSet.clone(binding.container)

            constraintSet.connect(binding.btnAddSpecialization.id, ConstraintSet.START, binding.container.id, ConstraintSet.START)
            constraintSet.connect(binding.btnAddSpecialization.id, ConstraintSet.END, binding.container.id, ConstraintSet.END)
            constraintSet.connect(
                binding.btnAddSpecialization.id,
                ConstraintSet.TOP,
                lastCreatedViewID,
                ConstraintSet.BOTTOM,
                resources.getDimensionPixelSize(app.slyworks.base_feature.R.dimen.layout_size_margin2))

            constraintSet.applyTo(binding.container)

            toggleEditStateForAllCustomLayout()
        }

        /* add a divider view after each custom EditText layout */
        val lp:ViewGroup.LayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1.px)
        val divider:View = View(context)
        divider.setLayoutParams(lp)
        divider.setId(View.generateViewId())

        binding.container.addView(layout)
        binding.container.addView(divider)

        constraintSet.connect(layout.id, ConstraintSet.START, binding.container.id, ConstraintSet.START)
        constraintSet.connect(layout.id, ConstraintSet.END, binding.container.id, ConstraintSet.END)
        constraintSet.connect(
            layout.id,
            ConstraintSet.TOP,
            lastCreatedViewID,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(app.slyworks.base_feature.R.dimen.layout_size_margin)
        )

        constraintSet.constrainWidth(layout.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(layout.id, ConstraintSet.WRAP_CONTENT)

        /* setting the anchor view for next created layout to this newly created layout */
        lastCreatedViewID = layout.id

        constraintSet.connect(divider.id, ConstraintSet.START, binding.container.id, ConstraintSet.START)
        constraintSet.connect(divider.id, ConstraintSet.END, binding.container.id, ConstraintSet.END)
        constraintSet.connect(
            divider.id,
            ConstraintSet.TOP,
            lastCreatedViewID,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(app.slyworks.base_feature.R.dimen.divider_top_margin)
        )

        constraintSet.constrainWidth(layout.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(layout.id, ConstraintSet.WRAP_CONTENT)

        /* setting the anchor view for next created layout */
        lastCreatedViewID = divider.id

        constraintSet.connect(binding.btnAddSpecialization.id, ConstraintSet.START, binding.container.id, ConstraintSet.START)
        constraintSet.connect(binding.btnAddSpecialization.id, ConstraintSet.END, binding.container.id, ConstraintSet.END)
        constraintSet.connect(
            binding.btnAddSpecialization.id,
            ConstraintSet.TOP,
            layout.id,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(app.slyworks.base_feature.R.dimen.layout_size_margin2)
        )

        /* apply all the changes to the host container */
        constraintSet.applyTo(binding.container)


        /* do an alpha animation to fade in the newly created layout */
        layout.alpha = 0F
        val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(
            layout,
            "alpha",
            0F,
            1F)
        objectAnimator.duration = 1_500
        objectAnimator.start()

        isThereCreatedLayout = true

        /* add it to list of views to be validated */
        createdLayoutsList.add(layout)

        toggleEditStateForAllCustomLayout()
    }

    private fun toggleEditStateForAllCustomLayout(){
        if(createdLayoutsList.isEmpty())
            return

        createdLayoutsList.forEachIndexed {index:Int, constraintLayout: View ->
            val editText:EditText = constraintLayout.findViewById(app.slyworks.base_feature.R.id.etText_layout_text)
            val imageView:ImageView = constraintLayout.findViewById(app.slyworks.base_feature.R.id.ivCancel)

            /* is the last editText layout, make 'x' imageView visible and
            make it editable*/
            if(index == createdLayoutsList.lastIndex){
                imageView.visibility = View.VISIBLE
                editText.keyListener = editText.tag as KeyListener
            }else {
                imageView.visibility = View.INVISIBLE

                if(editText.keyListener == null)
                    return

                editText.tag = editText.keyListener
                editText.keyListener = null

            }
        }

    }

}