package com.slyworks.medix.ui.fragments.homeFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.slyworks.medix.R


/**
 *Created by Joshua Sylvanus, 1:04 PM, 1/12/2022.
 */
class RVTrendingMedicalFieldsAdapter : RecyclerView.Adapter<RVTrendingMedicalFieldsAdapter.ViewHolder>() {
    //region Vars
    private val mList:MutableList<String> = data
    //endregion
    companion object{
        val data:MutableList<String> = mutableListOf(
            "General Medicine",
            "Cardiology",
            "Phyto-Medicine",
            "Natural Remedies",
            "Optometry",
            "Oncology",
            "Neurosurgery",
            "Physio-Therapy" )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View = LayoutInflater.from(parent.context).inflate(R.layout.li_trending_medical_fields, parent, false)
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
        private val rootView:CardView = itemView.findViewById(R.id.rootView)
        private val tvText: TextView = itemView.findViewById(R.id.tvTitle_trending_medical_fields)
        //endregion

        fun bind(text:String){
            tvText.text = text
        }
    }
}