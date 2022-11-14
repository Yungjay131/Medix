package com.slyworks.medix.ui.custom_views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.slyworks.medix.R
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject


/**
 *Created by Joshua Sylvanus, 7:39 PM, 21/10/2022.
 */
class SelectionItemView
@JvmOverloads
constructor(
    context: Context,
    attrs:AttributeSet? = null,
    defStyle:Int = 0,
    defStyleAttr:Int = 0): ConstraintLayout(context, attrs, defStyle, defStyleAttr) {
    //region Vars
    private var view:ConstraintLayout
    private var radioButton:RadioButton
    private var tvTitle:TextView
    private var tvSubtitle:TextView

    private var title:String
    private var subtitle:String
    private var isCurrentlySelected:Boolean = false

    private var subject:PublishSubject<Boolean> = PublishSubject.create()
    //endregion

    init{
        val a:TypedArray =
         context.theme.obtainStyledAttributes(attrs, R.styleable.SelectionItemView, 0, 0)

        try{
          title = a.getString(R.styleable.SelectionItemView_siv_title) ?: "not_set"
          subtitle = a.getString(R.styleable.SelectionItemView_siv_sub_title) ?: "not_set"
        }finally{
          a.recycle()
        }

        view = inflate(context, R.layout.selection_item, this) as ConstraintLayout
        radioButton = view.findViewById(R.id.siv_rb)
        tvTitle = view.findViewById(R.id.siv_title)
        tvSubtitle = view.findViewById(R.id.siv_sub_title)

        view.setOnClickListener {
            isCurrentlySelected = !isCurrentlySelected
            radioButton.isChecked = isCurrentlySelected
            if(isCurrentlySelected)
                view.setBackgroundResource(R.drawable.siv_background)
            else
                view.setBackgroundResource(R.drawable.siv_background_default)

            if(subject.hasObservers())
                subject.onNext(isCurrentlySelected)
        }
    }

    fun observeChanges(): Observable<Boolean> = subject.hide()
    fun setCurrentStatus(status:Boolean){ isCurrentlySelected = status }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        subject.onComplete()
    }
}