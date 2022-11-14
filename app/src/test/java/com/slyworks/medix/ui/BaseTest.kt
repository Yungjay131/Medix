package com.slyworks.medix.ui

import androidx.test.espresso.Espresso.setFailureHandler
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.slyworks.medix.ui.activities.login_activity.LoginActivity
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner


/**
 * Created by Joshua Sylvanus, 7:36 PM, 12/11/2022.
 */
//@RunWith(AndroidJUnit4::class)
@RunWith(RobolectricTestRunner::class)
class BaseTest {
    //region Vars
    //endregion

    /*@Rule
    val activityTestRule:ActivityTestRule<LoginActivity> = ActivityTestRule<>(LoginActivity::class.java)

    @Before
    fun setup(){
        setFailureHandler(CustomFailureHandler(
            InstrumentationRegistry.getInstrumentation().targetContext
        ))
    }*/

    /*
    * pressBack()
    * onData()
    * closeSoftKeyboard()
    * openContextualActionModeOverflowMenu()
    * openActionBarOverflowOptionsMenu()
    * */

    /*
    * @Test
public void objectMatcher() {
onView(not(isChecked()));
onView(allOf(withText("item 1"), isChecked()));
}
@Test
public void hierarchy() {
onView(withParent(withId(R.id.todo_item)));
onView(withChild(withText("item 2")));
onView(isDescendantOfA(withId(R.id.todo_item)));
onView(hasDescendant(isChecked()));
* onView(hasSibling(withContentDescription(R.string.menu_filter)));
}
@Test
public void input() {
onView(supportsInputMethods());
onView(hasImeAction(EditorInfo.IME_ACTION_SEND));
}
@Test
public void classMatchers() {
onView(isAssignableFrom(CheckBox.class));
onView(withClassName(is(FloatingActionButton.class.
getCanonicalName())));
}
@Test
public void rootMatchers() {
onView(isFocusable());
onView(withText(R.string.name_hint)).inRoot(isTouchable());
onView(withText(R.string.name_hint)).inRoot(isDialog());
onView(withText(R.string.name_hint)).inRoot(isPlatformPopup());
}
@Test
* public void preferenceMatchers() {
onData(withSummaryText("3 days"));
onData(withTitle("Send notification"));
onData(withKey("example_switch"));
onView(isEnabled());
}
@Test
public void layoutMatchers() {
onView(hasEllipsizedText());
onView(hasMultilineText());
}
    * onView(withId(R.id.viewId)).check(matches(withContentDescription(contains
String("YYZZ"))));
    *
    *
    * onView(withText(equalToIgnoringCase("xxYY"))).perform(click());

   *
   *
   * onView(withText(equalToIgnoringWhiteSpace("XX YY ZZ"))).perform(click());

   *
   * onView(withId(R.id.viewId)).check(matches(withText(not(containsString
("YYZZ")))));
*
* onView(withId(R.id.viewId))
.check(matches(allOf(withText(not(startsWith("ZZ"))),
withText(containsString("YYZZ")))));
*
* inRoot() : if there are 2 or more windows and you are interested
* in the parent
    * */
}