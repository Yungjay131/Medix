package com.slyworks.medix.ui.fragments.homeFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.slyworks.medix.R
import com.slyworks.medix.utils.ViewUtils.displayImage
import com.slyworks.models.models.AccountType


/**
 *Created by Joshua Sylvanus, 7:53 PM, 13/06/2022.
 */
data class QuickAction(val icon:Int, val text:String)
class RVQuickActionsAdapter(val type:AccountType) : RecyclerView.Adapter<RVQuickActionsAdapter.ViewHolder>(){
    //region Vars
    private val mList:MutableList<QuickAction> = mutableListOf()
    //endregion

    companion object{
        val data_doctor:MutableList<QuickAction> = mutableListOf(
            QuickAction(R.drawable.ic_chat, "Message"),
            QuickAction(R.drawable.ic_videocall, "Video Call"),
            QuickAction(R.drawable.ic_voice_call, "Voice Call"),
            QuickAction(R.drawable.ic_alarm_on, "Reminder"),
            QuickAction(R.drawable.ic_calendar, "Consultations"),
            QuickAction(R.drawable.ic_hospital, "Hospitals")
        )

        val data_patient:MutableList<QuickAction> = mutableListOf(
            QuickAction(R.drawable.ic_people, "Connect"),
            QuickAction(R.drawable.ic_diagnose, "Diagnose"),
            QuickAction(R.drawable.ic_chat, "Message"),
            QuickAction(R.drawable.ic_alarm_on, "Reminder"),
            QuickAction(R.drawable.ic_voice_call, "Voice Call"),
            QuickAction(R.drawable.ic_videocall, "Video Call")
        )
    }

    init {
        if(type == AccountType.DOCTOR) mList.addAll(data_doctor)
        else if(type == AccountType.PATIENT) mList.addAll(data_patient)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.li_quick_actions, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
       return mList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        //region Vars
        private val rootView:ConstraintLayout = itemView.findViewById(R.id.rootView)
        private val ivIcon:ImageView = itemView.findViewById(R.id.ivIcon_li_quick_action)
        private val tvText: TextView = itemView.findViewById(R.id.tvText_li_quick_actions)
        //endregion

        fun bind(entity:QuickAction){
            ivIcon.displayImage(entity.icon)
            tvText.setText(entity.text)
        }
    }
}