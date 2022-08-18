package com.slyworks.controller

data class Subscription(val event:String, val observer: Observer, var notifyMethod: NotifyMethod)