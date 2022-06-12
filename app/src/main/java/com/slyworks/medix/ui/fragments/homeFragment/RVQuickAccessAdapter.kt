package com.slyworks.medix.ui.fragments.homeFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.slyworks.medix.App
import com.slyworks.medix.R
import com.slyworks.models.models.AccountType
import com.slyworks.medix.utils.ViewUtils
import com.slyworks.medix.utils.ViewUtils.displayImage


/**
 *Created by Joshua Sylvanus, 12:21 PM, 1/12/2022.
 */
data class QuickAccessItem(var icon:Int, var title:String, var description:String)
class RVQuickAccessAdapter(var type: com.slyworks.models.models.AccountType) : RecyclerView.Adapter<RVQuickAccessAdapter.ViewHolder>(){
    //region Vars
    private val mList:MutableList<QuickAccessItem> = mutableListOf()
    //endregion
    companion object{
        val data_patient:MutableList<QuickAccessItem> = mutableListOf(
            QuickAccessItem(R.drawable.ic_people, "Connect", "Connect with various doctors from around the world" ),
            QuickAccessItem(R.drawable.ic_hospital, "Find Hospitals","Find hospitals close to you"),
            QuickAccessItem(R.drawable.ic_alarm_on, "Set Reminders for medication","Easily set reminders to take medications and for other health related events"),
            QuickAccessItem(R.drawable.ic_chat, "Chat","Easily communicate with doctors with the in-app real-time messaging feature"),
            QuickAccessItem(R.drawable.ic_videocall, "Video Consultation","Have real-time video consultaions"),
            QuickAccessItem(R.drawable.ic_voice_call, "Calls","Quickly contact doctors through the in-app voice call feature"),
            QuickAccessItem(R.drawable.ic_diagnose, "Check symptoms for common diseases","Easily diagnose symptoms for common illnesses")
        )
        val data_doctor:MutableList<QuickAccessItem> = mutableListOf(
            QuickAccessItem(R.drawable.ic_people, "Connect", "Connect with various patients from around the world" ),
            QuickAccessItem(R.drawable.ic_videocall, "Video Consultation","Have real-time video consultaions"),
            QuickAccessItem(R.drawable.ic_chat, "Chat","Easily communicate with patients with the in-app real-time messaging feature"),
            QuickAccessItem(R.drawable.ic_voice_call, "Calls","Quickly contact patients through the in-app voice call feature"),
            QuickAccessItem(R.drawable.ic_notify, "Notify nearby hospitals of patients conditions","Easily notify local hospitals of a patient's condition"),
            QuickAccessItem(R.drawable.ic_alarm_on, "Set Reminders for consultations","Easily set reminders patient consultations"),
            QuickAccessItem(R.drawable.ic_diagnose, "Organise and schedule","Organise and schedule patient consultations")
        )
    }

    init{
        if(type == com.slyworks.models.models.AccountType.PATIENT) mList.addAll(data_patient)
        else if(type == com.slyworks.models.models.AccountType.DOCTOR) mList.addAll(data_doctor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.li_quick_access, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
      holder.bind(mList.get(position))
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        //region Vars
        private val rootView:CardView = itemView.findViewById(R.id.rootView)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon_li_quick_access)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle_li_quick_access)
        private val tvDescription:TextView = itemView.findViewById(R.id.tvDescription_li_quick_access)
        //endregion

        fun bind(entity:QuickAccessItem){
            val color:Int = ViewUtils.getColor()
            rootView.setCardBackgroundColor(ContextCompat.getColor(App.getContext(), color))
            ivIcon.displayImage(entity.icon)
            tvTitle.text = entity.title
            tvDescription.text = entity.description
        }
    }
}