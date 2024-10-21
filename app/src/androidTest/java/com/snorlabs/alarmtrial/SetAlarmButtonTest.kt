package com.snorlabs.alarmtrial

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class SetAlarmButtonTest {

    @get:Rule val composeTestRule = createComposeRule()
    // use createAndroidComposeRule<YourActivity>() if you need access to
    // an activity

    @Test
    fun testSetAlarmButton() {

        val mockContext = InstrumentationRegistry.getInstrumentation().targetContext

        /*
        // Launch the app
        composeTestRule.setContent {
            AlarmTrialTheme {
                AlarmTrialLayout(mockContext)
            }
            
        }

         */
    }
}