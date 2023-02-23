package app.slyworks.controller_lib

class CompositeSubscription() : ISubscription{
   private val l:MutableList<Subscription> = mutableListOf()

   fun add(s: Subscription):Unit { l.add(s) }

   override fun clear():Unit{
      l.forEach{
         AppController.observers[it.event]?.remove(it)
         AppController.eventMap[it.event]?.second?.remove(it)
      }
   }

   override fun clearAndRemove():Unit {
      l.forEach {
         if (AppController.observers[it.event] == null) return

         if (AppController.observers[it.event]!!.contains(it))
            AppController.observers[it.event]!!.remove(it)

         if (AppController.eventMap[it.event]?.second?.contains(it) == true)
            AppController.eventMap.remove(it.event)

         AppController.removeEvent(it.event)
      }
   }
}

