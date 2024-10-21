package com.snorlabs.alarmtrial

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

@SuppressLint("ScheduleExactAlarm")
fun setAlarm(context: Context, hour: Int, minute: Int) {

    val TAG = "setAlarm"
    Log.d(TAG,"In Alarm Setter")

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    // Extra used for notification message. When alarm goes off, AlarmReceiver is triggered.
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

    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)

        if(this.timeInMillis <= System.currentTimeMillis()){
            add(Calendar.DAY_OF_YEAR,1)

            // Check to see if the it is the first day of the year to resolve new year issue
            if(get(Calendar.DAY_OF_YEAR) == 1) {
                add(Calendar.YEAR, 1)
            }
        }
    }

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        alarmPendingintent
    )

}