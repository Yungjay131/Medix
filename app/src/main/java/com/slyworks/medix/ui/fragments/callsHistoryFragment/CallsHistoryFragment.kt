package com.slyworks.medix.ui.fragments.callsHistoryFragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.slyworks.medix.R
import com.slyworks.medix.ui.activities.main_activity.activityComponent
import com.slyworks.medix.ui.custom_views.CustomDividerDecorator
import javax.inject.Inject

class CallsHistoryFragment : Fragment() {
    //region Vars
    private lateinit var rootView:CoordinatorLayout
    private lateinit var progress:ProgressBar
    private lateinit var rvCallsHistory:RecyclerView
    private lateinit var errorLayout:ConstraintLayout
    private lateinit var tvError:TextView
    private lateinit var fabStartCalls:FloatingActionButton

    private lateinit var mAdapter: RVCallsHistoryAdapter

    @Inject
   lateinit var mViewModel:CallsHistoryViewModel
    //endregion

    companion object {
        @JvmStatic
        fun newInstance(): CallsHistoryFragment  = CallsHistoryFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        context.activityComponent
            .fragmentComponentBuilder()
            .setFragment(this)
            .build()
            .inject(this)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_calls_history, container, false)
        initViews1(view)
        initData()
        return view
    }

    private fun initViews1(view:View){
        rootView = view.findViewById(R.id.rootView)
        progress = view.findViewById(R.id.progress_layout)
        rvCallsHistory = view.findViewById(R.id.rvCalls_calls_history)
        errorLayout = view.findViewById(R.id.errorLayout_frag_calls_history)
        tvError = view.findViewById(R.id.tvError_frag_calls_history)
        fabStartCalls = view.findViewById(R.id.fab_start_new_call)

        mAdapter = RVCallsHistoryAdapter()
        rvCallsHistory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvCallsHistory.addItemDecoration(
            CustomDividerDecorator<MaterialDivider>(id = R.id.divider_horizontal_1))
        rvCallsHistory.adapter = mAdapter
    }

    private fun initData(){
        mViewModel.progressState
            .observe(viewLifecycleOwner){
            progress.isVisible = it
        }
        mViewModel.errorState
            .observe(viewLifecycleOwner){
            errorLayout.isVisible = it
            if(it)
                tvError.text = mViewModel.errorData.value
        }
        mViewModel.observeCallsHistory()
            .observe(viewLifecycleOwner){
            mAdapter.submitList(it)
        }
    }
}