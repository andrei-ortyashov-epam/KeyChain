package ru.andrort.keychain

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

	@Rule
	@JvmField
	val activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

	@Test
	fun checkKeyChain() {
		for (i in 0..3) {
			val initial = "${UUID.randomUUID().toString()}-$i"
			onView(withId(R.id.initialText)).perform(clearText()).perform(typeText(initial))
			onView(withId(R.id.generate)).perform(click())
			onView(withId(R.id.decryptedText)).check(matches(withText(initial)))
		}
	}
}
