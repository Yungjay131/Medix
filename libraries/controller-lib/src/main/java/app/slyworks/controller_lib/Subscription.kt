package app.slyworks.controller_lib

data class Subscription(
    val event:String,
    val observer: Observer,
    var notifyMethod: NotifyMethod) : ISubscription{

    override fun clear() {
        AppController.observers[this.event]?.remove(this)
        AppController.eventMap[this.event]?.second?.remove(this)
    }

    override fun clearAndRemove() {
        if (AppController.observers[this.event] == null) return

        if (AppController.observers[this.event]!!.contains(this))
            AppController.observers[this.event]!!.remove(this)

        if (AppController.eventMap[this.event]?.second?.contains(this) == true)
            AppController.eventMap.remove(this.event)

        AppController.removeEvent(this.event)
    }
}