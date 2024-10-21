package com.snorlabs.alarmtrial

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        // Receiver functionality required for dynamic alarm to start alarm service.
        // Makes it easier to cancel all alarms (Regular and dynamic) as a service.
        // To be reviewed once dynamic alarm function. Possibly removed.

        val TAG = "AlertReceiver"
        Log.d(TAG,"In Receiver")

        val serviceIntent = Intent(context,AlarmService::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context?.startService(serviceIntent)

    }
}