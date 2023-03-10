package com.game.rps;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class StatisticViewingTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);
    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Test
    public void statisticViewingTest() {
        // navigate to statistics
        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.navigation_info),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0),
                                2),
                        isDisplayed()));
        // check that all components of the statistics are displayed
        bottomNavigationItemView.perform(click());
        onView(withId(R.id.loses_text)).
                    check(matches(isDisplayed()));
        onView(withId(R.id.losesPlayer_text)).
                check(matches(isDisplayed()));
        onView(withId(R.id.wins_text)).
                check(matches(isDisplayed()));
        onView(withId(R.id.winsPlayer_text)).
                check(matches(isDisplayed()));
        onView(withId(R.id.rounds_text)).
                check(matches(isDisplayed()));
        onView(withId(R.id.roundsPlayer_text)).
                check(matches(isDisplayed()));
        onView(withId(R.id.nickname_text)).
                check(matches(isDisplayed()));
        onView(withId(R.id.nicknamePlayer_text)).
                check(matches(isDisplayed()));

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
}
