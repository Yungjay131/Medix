package com.slyworks.medix.ui.fragments.chatFragment

import androidx.recyclerview.widget.DiffUtil
import com.slyworks.models.room_models.Person

/**
 *Created by Joshua Sylvanus, 5:47 AM, 28/04/2022.
 */
class MDiffUtilCallback : DiffUtil.ItemCallback<Person>(){
    override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
        return oldItem.firebaseUID == newItem.firebaseUID
    }

    override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean {
        return oldItem == newItem
    }
}