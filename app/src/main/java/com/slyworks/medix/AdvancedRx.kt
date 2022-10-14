package com.slyworks.medix

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit


/**
 *Created by Joshua Sylvanus, 8:56 PM, 10/09/2022.
 */
class AdvancedRx {
    //region Vars
    //endregion
    private fun getRequestObservable():Observable<String> = Observable.just("that's whatsup")
    private fun retryingFailedOp(){
        /* this code retries the request 3 times, with a 5s delay, then 10s, then 15s */
        /* zipWith only emits when both observable have values ???
        * hence the attempt */
        getRequestObservable()
                /* roadblock for errors, after 3 times passes to onError() */
            .retryWhen { attempt:Observable<Throwable> ->
                attempt
                    .zipWith(Observable.range(1,3), { time,index -> index})
                    .flatMap{ index ->
                        Observable.timer(5L * index, TimeUnit.SECONDS)
                    }
            }
    }

    private val retryRequest:PublishSubject<Long> = PublishSubject.create()

    private fun retryingFailedOp2(){
        /* when the request fails, return the retryRequest PublishRelay
        * because what retrywhen does is it returns an observable to be subscribed to */
        getRequestObservable()
            .retryWhen { attempt:Observable<Throwable> -> retryRequest }
            .subscribe({
                       //handle updated response
            },{
                //retryView.setVisibility(View.VISIBLE)
            })

        /* btnRetry.setOnClickListener{ retryRequest.call(System.currentTimeInMillis() }*/
    }
}