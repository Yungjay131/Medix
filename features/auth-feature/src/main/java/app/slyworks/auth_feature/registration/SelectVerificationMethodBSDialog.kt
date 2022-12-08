package app.slyworks.auth_feature.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.slyworks.auth_feature.databinding.BottomsheetSelectVerificationMethodBinding
import app.slyworks.auth_lib.VerificationDetails
import app.slyworks.utils_lib.utils.plusAssign
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject


/**
 * Created by Joshua Sylvanus, 9:27 PM, 11/11/2022.
 */
class SelectVerificationMethodBSDialog : app.slyworks.base_feature.BaseBottomSheetDialogFragment() {
    //region Vars
    private lateinit var binding: BottomsheetSelectVerificationMethodBinding
    private val disposables:CompositeDisposable = CompositeDisposable()
    private var subject:PublishSubject<app.slyworks.auth_lib.VerificationDetails> = PublishSubject.create()
    //endregion

    companion object{
        @JvmStatic
        fun getInstance(): SelectVerificationMethodBSDialog = SelectVerificationMethodBSDialog()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomsheetSelectVerificationMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initViews()
    }

    private fun initData(){}
    private fun initViews(){
        var selected: VerificationDetails = VerificationDetails.EMAIL

        disposables +=
        Observable.combineLatest(binding.sivEmail.observeChanges(),
                                 binding.sivOtp.observeChanges(),
            { oEmail:Boolean, oOTP:Boolean ->
                return@combineLatest when {
                    oEmail -> VerificationDetails.EMAIL
                    oOTP -> VerificationDetails.OTP
                    else -> throw UnsupportedOperationException()
                }
            })
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                when(it){
                    VerificationDetails.EMAIL -> binding.sivOtp.setCurrentStatus(false)
                    VerificationDetails.OTP -> binding.sivEmail.setCurrentStatus(false)
                }

                selected = it
                binding.btnProceed.isEnabled = true
            }

        binding.btnProceed.setOnClickListener {
            subject.onNext(selected)
        }
    }

    fun getSubject():Observable<VerificationDetails> = subject.hide()
}