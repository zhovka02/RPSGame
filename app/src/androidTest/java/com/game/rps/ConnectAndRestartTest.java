package com.game.rps;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.game.rps.CustomDrawableMatchers.withDrawableId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.game.rps.controll.game.GameControllerImpl;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Run on only two devices at the same time using the built-in Android Studio function
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class ConnectAndRestartTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Test
    public void connectAndRestartTest() {
        // give time for all components to initialise
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // enable detection by another device
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.discOnOff), withText("on/off"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_host_fragment_activity_main),
                                        0),
                                1),
                        isDisplayed()));
        appCompatButton.perform(click());
        // give time for the permission dialogue to appear
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // allow detection
        if (Build.VERSION.SDK_INT >= 23) {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            UiObject allowPermissions = device.findObject(new UiSelector().text("ALLOW"));
            UiObject allowPermissions2 = device.findObject(new UiSelector().text("Allow"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException ignored) {
                }
            }
            if (allowPermissions2.exists()) {
                try {
                    allowPermissions2.click();
                } catch (UiObjectNotFoundException ignored) {
                }
            }
        }
        // let's start the search
        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.btnFindUnpairedDevices), withText("Search"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_host_fragment_activity_main),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton2.perform(click());
        // give time for the device to be detected
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DataInteraction linearLayout = onData(anything())
                .inAdapterView(allOf(withId(R.id.lvNewDevices),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                3)))
                .atPosition(0);
        linearLayout.perform(click());
        // give time for connection
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // let's move on into the game fragment
        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.navigation_game),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0),
                                1),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());
        // check that it shows us that we are now connected to a opponent
        onView(allOf(withId(R.id.gameText), withText(
                String.format(this.getResources().getString(R.string.connected),
                        GameControllerImpl.getController().getOpponent().getName())))).
                check(matches(isDisplayed()));
        onView(withId(R.id.gameStateImage)).
                check(matches(withDrawableId(R.drawable.ready_image)));
        // let's move on into the settings fragment
        ViewInteraction bottomNavigationItemView2 = onView(
                allOf(withId(R.id.navigation_settings),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0),
                                0),
                        isDisplayed()));
        bottomNavigationItemView2.perform(click());
        // reset the connection to the current opponent
        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.reset), withText("reset"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_host_fragment_activity_main),
                                        0),
                                4),
                        isDisplayed()));
        appCompatButton3.perform(click());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // let's move on into the game fragment
        ViewInteraction bottomNavigationItemView3 = onView(
                allOf(withId(R.id.navigation_game),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0),
                                1),
                        isDisplayed()));
        bottomNavigationItemView3.perform(click());
        // check that it shows us that we are now not connected to a opponent
        onView(allOf(withId(R.id.gameText),
                withText(this.getResources().getString(R.string.not_connected)))).
                check(matches(isDisplayed()));
        onView(withId(R.id.gameStateImage)).
                check(matches(withDrawableId(R.drawable.not_connnected_image)));
        // let's move on into the settings fragment
        ViewInteraction bottomNavigationItemView4 = onView(
                allOf(withId(R.id.navigation_settings),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0),
                                0),
                        isDisplayed()));
        bottomNavigationItemView4.perform(click());
        // search again and connect to the opponent
        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.btnFindUnpairedDevices), withText("Search"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_host_fragment_activity_main),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton4.perform(click());
        // give time for the device to be detected
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DataInteraction linearLayout2 = onData(anything())
                .inAdapterView(allOf(withId(R.id.lvNewDevices),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                3)))
                .atPosition(0);
        linearLayout2.perform(click());
        // give time for connection
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // let's move on into the game fragment
        ViewInteraction bottomNavigationItemView5 = onView(
                allOf(withId(R.id.navigation_game),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0),
                                1),
                        isDisplayed()));
        bottomNavigationItemView5.perform(click());
        // check that it shows us that we are now connected to a opponent
        onView(allOf(withId(R.id.gameText), withText(
                String.format(this.getResources().getString(R.string.connected),
                        GameControllerImpl.getController().getOpponent().getName())))).
                check(matches(isDisplayed()));
        onView(withId(R.id.gameStateImage)).
                check(matches(withDrawableId(R.drawable.ready_image)));
    }
    private Resources getResources() {
        Context targetContext =
                InstrumentationRegistry.getInstrumentation().getTargetContext();

        return targetContext.getResources();
    }
}
