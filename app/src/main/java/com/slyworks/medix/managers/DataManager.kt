package com.slyworks.medix.managers

import android.util.Log
import androidx.collection.SimpleArrayMap
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.slyworks.constants.*
import com.slyworks.data.AppDatabase
import com.slyworks.medix.*
import com.slyworks.medix.utils.MChildEventListener
import com.slyworks.medix.utils.MValueEventListener
import com.slyworks.medix.utils.UserDetailsUtils
import com.slyworks.models.models.Outcome
import com.slyworks.models.room_models.Message
import com.slyworks.models.room_models.Person
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest


class DataManager{
    //region Vars
    private val TAG: String? = DataManager::class.simpleName

    //endregion
}