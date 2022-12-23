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
import static org.mockito.Mockito.mock;

import android.app.UiAutomation;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
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
import com.game.rps.controll.game.State;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ConnectAndPerformRoundTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    private Boolean isReadyToPerformRound(){
        return GameControllerImpl.getController().getChoice() != null && GameControllerImpl.getController().getState() == State.CONNECTED;
    }

    @Test
    public void connectAndPerformRoundTest()  {
        // give time for all components to initialise
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // create a mock-GameController that does not require the phone to shake in order to make
        // a round. This is necessary because there is no software way to make the phone under test
        // think it is being shaken.
        GameControllerImpl mock = Mockito.spy(GameControllerImpl.class);
        Mockito.when(mock.isReadyToPerformRound()).thenAnswer((Answer<Boolean>) invocation -> this.isReadyToPerformRound());
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
            UiObject allowPermissions = device.findObject(new UiSelector().text("РАЗРЕШИТЬ"));
            UiObject allowPermissions2 = device.findObject(new UiSelector().text("Разрешить"));
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
        // simulate pressing the volume key
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        this.sendKey(KeyEvent.KEYCODE_VOLUME_UP, uiAutomation);
        // give time to process the press
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // give time to get answer from opponent
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // check that it shows us that we are in draw state
        onView(allOf(withId(R.id.gameText),
                withText(this.getResources().getString(R.string.draw)))).
                check(matches(isDisplayed()));
        onView(withId(R.id.gameStateImage)).
                check(matches(withDrawableId(R.drawable.draw)));
        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.restartButton), withText("restart"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_host_fragment_activity_main),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton3.perform(click());

        // check that it shows us that we are now connected to a opponent
        onView(allOf(withId(R.id.gameText), withText(
                String.format(this.getResources().getString(R.string.connected),
                        GameControllerImpl.getController().getOpponent().getName())))).
                check(matches(isDisplayed()));
        onView(withId(R.id.gameStateImage)).
                check(matches(withDrawableId(R.drawable.ready_image)));
        // give time to get be connected
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendKey(int keyCode, UiAutomation uiAutomation) {
        this.sendKeyEvent(keyCode, KeyEvent.ACTION_DOWN, uiAutomation);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.sendKeyEvent(keyCode, KeyEvent.ACTION_UP, uiAutomation);
    }

    private void sendKeyEvent(int keyCode, int action, UiAutomation uiAutomation) {
        long downTime = SystemClock.uptimeMillis();
        KeyEvent event = new KeyEvent(
                downTime,
                downTime,
                action,
                keyCode,
                0,
                0,
                KeyCharacterMap.VIRTUAL_KEYBOARD,
                0,
                KeyEvent.FLAG_FROM_SYSTEM,
                InputDevice.SOURCE_KEYBOARD
        );

        uiAutomation.injectInputEvent(event, true);

    }

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
    private Resources getResources() {
        Context targetContext =
                InstrumentationRegistry.getInstrumentation().getTargetContext();

        return targetContext.getResources();
    }
}
