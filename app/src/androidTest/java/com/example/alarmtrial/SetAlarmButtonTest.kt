package com.example.alarmtrial

import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.example.alarmtrial.ui.theme.AlarmTrialTheme
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