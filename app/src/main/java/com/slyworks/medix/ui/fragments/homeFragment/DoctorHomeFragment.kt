package com.slyworks.medix.ui.fragments.homeFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.slyworks.medix.R
import de.hdodenhof.circleimageview.CircleImageView

class DoctorHomeFragment : Fragment() {
    //region Vars
    private lateinit var tvUserName: TextView
    private lateinit var ivNotification: CircleImageView
    private lateinit var tvNotificationCount: TextView

    private lateinit var searchView: SearchView
    private lateinit var rvQuickActions: RecyclerView

    private lateinit var cardCovid: MaterialCardView

    private lateinit var rvHealthAreas: RecyclerView

    private lateinit var tvUpcomingConsultation: TextView

    private lateinit var cardSchedule: MaterialCardView
    private lateinit var ivProfile_cardSchedule: CircleImageView
    private lateinit var tvName_cardSchedule: TextView
    private lateinit var tvDate_cardSchedule: TextView
    private lateinit var tvTime_cardSchedule: TextView
    //endregion

    companion object {
        @JvmStatic
        fun getInstance() = DoctorHomeFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_doctor, container, false)
    }


}