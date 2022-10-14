package com.slyworks.communication

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable


/**
 *Created by Joshua Sylvanus, 8:56 PM, 19/08/2022.
 */

internal operator fun CompositeDisposable.plusAssign(d: Disposable){
    this.add(d)
}