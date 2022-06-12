package com.slyworks.medix.concurrency

import androidx.collection.SimpleArrayMap
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 *Created by Joshua Sylvanus, 2:16 AM, 1/3/2022.
 */
object TaskManager {
    //region Vars
    private val NUMBER_OF_CORES:Int = Runtime.getRuntime().availableProcessors()
    private var mExecutorsMap:SimpleArrayMap<Long,ExecutorService>? = SimpleArrayMap()
    //endregion

    fun newSingleThreadExecutor(executorID:Long): ExecutorService {
        val e =  Executors.newSingleThreadExecutor()
        mExecutorsMap!!.put(executorID,e)
        return e
    }

    fun newThreadPoolExecutor(executorID: Long): ExecutorService{
        val e = Executors.newFixedThreadPool(NUMBER_OF_CORES)
        mExecutorsMap!!.put(executorID, e)
        return e
    }

    fun destroyExecutor(executorID: Long){
       mExecutorsMap?.get(executorID)?.shutdownNow()
    }

    fun runOnSingleThread(task:Runnable){
        val e = Executors.newSingleThreadExecutor()
        e.submit(task)
        e.shutdown()
    }

    fun <T, R, S>runOnSingleThread(p1:R?, p2:S?, task:(r:R?, s:S?)->T):T{
        val e = Executors.newSingleThreadExecutor()
        val data = e.submit(
            Callable<T>{
                task(p1, p2)
            }).get()
        e.shutdownNow()
        return data
    }

    fun <T>runOnSingleThread(task:Callable<T>):T{
        val e = Executors.newSingleThreadExecutor()
        return execute<T>(e,task)
    }

    fun runOnThreadPool(task:Runnable){
        val e = Executors.newFixedThreadPool(NUMBER_OF_CORES)
        e.submit(task)
        e.shutdown()
    }

    fun <T>runOnThreadPool(task: Callable<T>):T{
        return Executors.newFixedThreadPool(NUMBER_OF_CORES).submit(task).get()
    }

    //fixme:not sure creating THREADS 2x the NUM_OF_CORES is a good idea.find a better way
    fun <T>runOnComputationThreadPool(task:Callable<T>):T{
        val e = Executors.newFixedThreadPool(NUMBER_OF_CORES * 2)
        return execute<T>(e,task)
    }

    private fun <T>execute(e:ExecutorService, task:Callable<T>):T{
        val data = e.submit(task).get()
        e.shutdownNow()
        return data
    }

    fun handleUncompletedTasks(){
        /*TODO:first check if there are uncomplete tasks like DeferredMessages, FCMRegistrationTokens not sent from server
        * profile update, not yet implemented etc*/



    }
}