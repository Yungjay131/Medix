package com.slyworks.medix.ui.fragments.findDoctorsFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDivider
import com.slyworks.constants.EVENT_OPEN_VIEW_PROFILE_FRAGMENT
import com.slyworks.medix.App
import com.slyworks.medix.AppController
import com.slyworks.medix.R
import com.slyworks.medix.utils.ViewUtils.displayImage
import com.slyworks.models.room_models.FBUserDetails
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


/**
 *Created by Joshua Sylvanus, 4:54 AM, 1/8/2022.
 */
class RVFindDoctorsAdapter : RecyclerView.Adapter<RVFindDoctorsAdapter.ViewHolder>() {
    //region Vars
    private val mList:MutableList<FBUserDetails> = mutableListOf()
    //endregion
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.li_find_doctors, parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doctor = mList.get(position)
        holder.bind(doctor)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun addDoctors(list:MutableList<FBUserDetails>){
        val startIndex = if(mList.isEmpty()) 0 else mList.size - 1
        mList.addAll(list)

        notifyItemRangeInserted(startIndex, list.size)
    }

    fun addDoctor(doctor:FBUserDetails){
        val index = mList.size - 1
        mList.add(doctor)

        notifyItemInserted(index)
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        //region Vars
        private val TAG: String? = RVFindDoctorsAdapter::class.simpleName

        private val ivProfile:CircleImageView =itemView.findViewById(R.id.ivProfile_find_doctor)
        private val tvDoctorName: TextView = itemView.findViewById(R.id.tvName_find_doctor)
        private val tvSpecialisation: TextView = itemView.findViewById(R.id.tvSpecialization_find_doctor)
        //endregion
        fun bind(entity: FBUserDetails){
             ivProfile.displayImage(entity.imageUri)

             val firstName = entity.firstName
             val secondName = entity.lastName
             val _firstName = firstName.substring(0,1)
                 .uppercase(Locale.getDefault())
                                     .plus(firstName.substring(1,firstName.length))
            val _secondName = secondName.substring(0, 1)
                .uppercase(Locale.getDefault())
                                        .plus(secondName.substring(1, secondName.length))
            val fullName:String = "$_firstName $_secondName"
             tvDoctorName.text = "Dr. $fullName"

            val sb:StringBuilder = StringBuilder()
            entity.specialization?.forEach{
                if(it != entity.specialization!!.last()){
                    sb.append("${it},")
                }

                sb.append(it)
            }
            tvSpecialisation.text = App.getContext().getString(R.string.specialization_holder, sb.toString())

            itemView.setOnClickListener {
                AppController.notifyObservers(EVENT_OPEN_VIEW_PROFILE_FRAGMENT, entity)
            }

        }
    }
}