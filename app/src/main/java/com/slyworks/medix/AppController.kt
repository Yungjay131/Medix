package com.slyworks.medix

import com.slyworks.models.models.NotifyMethod
import com.slyworks.models.models.Observer
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject


/**
 *Created by Joshua Sylvanus, 11:56 AM, 12/10/2021.
 */

data class DataHolder(var data:Any)
data class Subscription(val event:String, val observer: Observer, var notifyMethod: NotifyMethod)
/*TODO:create something similar to CompositeDisposable*/
data class CompositeSubscription(var s: Subscription? = null){}
object AppController {
    //region Vars
    private var mEvents:MutableList<String> = mutableListOf();
    private var mObservers:MutableMap<String, MutableSet<Subscription>> = mutableMapOf()
    private var mEventMap:MutableMap<String, Pair<DataHolder,MutableSet<Subscription>>> = mutableMapOf();
    //endregion


    private fun addEvent(event:String){
        if(mEvents.contains(event)) return

        mEvents.add(event)
        mObservers[event] = mutableSetOf()
    }
    private fun removeEvent(event:String){
        mEvents.remove(event)
        mObservers.remove(event)
        mEventMap.remove(event)
    }

    fun subscribeTo(event:String, observer: Observer, notifyMethod: NotifyMethod = NotifyMethod.PUSH_IMMEDIATELY): Subscription {
        if(!mEvents.contains(event))
            addEvent(event)

        val s = Subscription(event, observer, notifyMethod)
        mObservers[event]!!.add(s)
        return s
    }

    fun Subscription.clear(){
        mObservers[this.event]?.remove(this)
        mEventMap[this.event]?.second?.remove(this)
    }

    fun Subscription.clearAndRemove(){
        if(mObservers[this.event] == null) return

        if(mObservers[this.event]!!.contains(this))
           mObservers[this.event]!!.remove(this)

        if(mEventMap[this.event]?.second?.contains(this) == true)
            mEventMap.remove(this.event)

        removeEvent(this.event)
    }

    fun unsubscribe(subscription: Subscription){
        mObservers[subscription.event]!!.remove(subscription)
    }

    fun <T>notifyObservers(event:String, data:T){
        //Log.e("AppController", "notifyObservers: ${Thread.currentThread().name}" )

        val observers:MutableSet<Subscription>? = mObservers[event]
        if(observers.isNullOrEmpty()) return

        observers.forEach { subscriber: Subscription ->
            if (subscriber.notifyMethod == NotifyMethod.PUSH_IMMEDIATELY)
                subscriber.observer.notify(event, data)

            /*to avoid caching unnecessarily, only cache if there is a "cold" Observer subscribed to event*/
            if(subscriber.notifyMethod == NotifyMethod.WAIT_FOR_PULL)
                cacheData(event, subscriber, data as Any)
        }
    }

    private fun cacheData(event:String, subscriber: Subscription, data:Any){
        mEventMap[event]?.let{
            it.first.data = data
            it.second.add(subscriber)
            return
        }

        mEventMap[event] = Pair<DataHolder,MutableSet<Subscription>>(
            DataHolder(data), mutableSetOf(subscriber) )
    }

    fun <T>pullData(event:String,observer: Observer):T{
        return mEventMap[event]?.first?.data as T
    }

    private var mTopicList:MutableList<String> = mutableListOf()
    private var mTopicObservers:MutableMap<String, PublishSubject<Any>> = mutableMapOf()
    fun addTopic(topic:String){
        if(mTopicList.contains(topic))
            return

        mTopicList.add(topic)
        mTopicObservers.put(topic, PublishSubject.create<Any>())
    }

    fun <T> pushToTopic(topic:String, data:T){
        mTopicObservers.get(topic)?.onNext(topic)
    }

    fun <T> subscribeTo(topic:String): Observable<T>{
        if(!mTopicList.contains(topic))
            addTopic(topic)

        return mTopicObservers.get(topic)!!.hide() as Observable<T>
    }
}