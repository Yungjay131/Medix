package app.slyworks.auth_feature.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.slyworks.auth_feature.databinding.BottomsheetSelectVerificationMethodBinding
import app.slyworks.base_feature.BaseBottomSheetDialogFragment
import app.slyworks.data_lib.model.models.VerificationDetails
import app.slyworks.utils_lib.utils.plusAssign
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject


/**
 * Created by Joshua Sylvanus, 9:27 PM, 11/11/2022.
 */
class SelectVerificationMethodBSDialog : BaseBottomSheetDialogFragment() {
    //region Vars
    private var subject:PublishSubject<VerificationDetails> = PublishSubject.create()
    private val disposables:CompositeDisposable = CompositeDisposable()
    private lateinit var binding: BottomsheetSelectVerificationMethodBinding
    //endregion

    companion object{
        @JvmStatic
        fun newInstance(): SelectVerificationMethodBSDialog = SelectVerificationMethodBSDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomsheetSelectVerificationMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews(){
        var selected: VerificationDetails = VerificationDetails.EMAIL

        disposables +=
        binding.sivEmail.observeChanges()
            .subscribe{
                if(it){
                    binding.sivOtp.setCurrentStatus(false)
                    selected = VerificationDetails.EMAIL
                }
            }

        disposables +=
        binding.sivOtp.observeChanges()
            .subscribe{
                if(it){
                    binding.sivEmail.setCurrentStatus(false)
                    selected = VerificationDetails.OTP
                }
            }

        disposables +=
        Observable.combineLatest(binding.sivEmail.observeChanges(),
                                 binding.sivOtp.observeChanges(),
            { isEmail:Boolean, isOTP:Boolean ->
                return@combineLatest isEmail || isOTP
            })
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(binding.btnProceed::setEnabled)

        binding.btnProceed.setOnClickListener {
            subject.onNext(selected)
        }
    }

    fun getSubject():Observable<VerificationDetails> = subject.hide()
}