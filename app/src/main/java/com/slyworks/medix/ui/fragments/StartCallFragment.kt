package com.slyworks.medix.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slyworks.medix.R


class StartCallFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start_call, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = StartCallFragment()
    }
}