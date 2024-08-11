package com.example.alarmtrial

import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.alarmtrial.ui.theme.AlarmTrialTheme
import org.w3c.dom.Text
import java.util.Calendar

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlarmContent(this)
        }
    }
}

@Composable
fun AlarmContent(activity: ComponentActivity) {

    Box(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        , contentAlignment = Alignment.TopCenter) {
        Column(
            horizontalAlignment =
            Alignment.CenterHorizontally
        ) {

            Text(text = "Alarm Activity")

            //Spacer to push the button to bottom of screen
            Spacer(modifier = Modifier.weight(1f))

            // Snooze Alarm
            Button(
                onClick = {
                    snoozeAlarm(activity)
                })
            {
                Text(text = "Snooze")
            }

            //Spacer
            Spacer(modifier = Modifier.padding(8.dp))

            // Go back to MainActivity
            Button(
                onClick = {

                    shutDownAlarm(activity)

                })
            {
                Text(text = "Go Home")
            }
        }
    }
}

fun shutDownAlarm(context: Context) {

    //Cancels the alarm service. OnDestroy, the service will shut down.
    val serviceIntent = Intent(context,AlarmService::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.stopService(serviceIntent)

    // Cancels the pending intent of regular alarm if dynamic goes off
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Create pending intent with same name and requestCode to cancel.
    val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("NotificationMessage", "Test")
    }

    val alarmPendingintent = PendingIntent
        .getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    alarmManager.cancel(alarmPendingintent)

    // Cancels the Countdown Service if regular alarm goes off
    // Stop the countdown service
    val intent = Intent(context, CountdownTimer::class.java)
    context.stopService(intent)

    //Close Notification
    val notificationManager =
        context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(1)

    //Back home
    val homeIntent = Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(homeIntent)
}

fun snoozeAlarm(context: Context){
    // First cancel the service
    // Cancels the alarm service. OnDestroy, the service will shut down.
    val serviceIntent = Intent(context,AlarmService::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.stopService(serviceIntent)

    //Close Notification
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(1)

    // Get the current time
    val calendar = Calendar.getInstance()
    val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
    var minute = calendar.get(Calendar.MINUTE)

    // Set the alarm to 1 minute from now
    minute = minute + 1

    // Set new alarm
    setAlarm(context,hourOfDay,minute)
}
