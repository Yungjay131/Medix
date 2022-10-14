package com.slyworks.medix

import arrow.core.compose
import arrow.core.curried
import arrow.core.partially1


/**
 *Created by Joshua Sylvanus, 11:15 AM, 24/09/2022.
 */
class Arrow {
  private val uncurried:((Int) -> Boolean, List<Int>) -> List<Int>
          = { func, list -> list.filter{ it:Int -> func(it) } }

  private val filter:((Int) -> Boolean, List<Int>) -> List<Int>
          = { func, list -> list.filter { it: Int -> func(it) } }

  private val filter2: ((Int) -> Boolean) -> (List<Int>) -> List<Int>
          = uncurried.curried()

  private val takeEvens: (List<Int>) -> List<Int>
          = filter.partially1 { it % 2 == 0 }

  private val takeGreaterThanThree: (List<Int>) -> List<Int>
          = filter.partially1 { it > 3 }

  private val takeEvensGreaterThanThree: (List<Int>) -> List<Int>
          = takeGreaterThanThree.compose(takeEvens)

  /*private val takeEvensGreaterThanThree2:(List<Int>) -> List<Int>
          = { it:List<Int> -> it.pipe(takeEvens).pipe(takeGreaterThanThree) }*/

}