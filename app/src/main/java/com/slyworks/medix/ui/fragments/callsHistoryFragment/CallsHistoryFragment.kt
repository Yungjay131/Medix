package com.slyworks.medix.ui.fragments.callsHistoryFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slyworks.medix.R

class CallsHistoryFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(): CallsHistoryFragment {
            return CallsHistoryFragment()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calls_history, container, false)
    }



}