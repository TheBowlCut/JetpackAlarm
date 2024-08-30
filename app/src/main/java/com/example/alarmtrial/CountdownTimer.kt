package com.example.alarmtrial

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.SleepClassifyEvent
import com.google.android.gms.location.SleepSegmentRequest
import java.util.concurrent.TimeUnit

class CountdownTimer : Service() {

    // Companion object - Construct that allows you to define static members and functions
    // for a class. Singleton, so only one instance of it. It can be accessed using the class name,
    // without needing an instance of the class.
    // Used here to define constants that can be easily accessed across different parts of the code.
    // This is useful for defining broadcast actions and intent extra keys,
    // avoiding hardcoding multiple times

    companion object {
        const val COUNTDOWN_BR = "com.countdownservicecomp.countdown_br"
        const val EXTRA_TIME_REMAINING = "time_remaining"
        const val EXTRA_PAUSE_STATE = "extra_pause_state"
        const val ACTION_START = "com.countdownservicecomp.START"
        const val ACTION_PAUSE = "com.countdownservicecomp.PAUSE"
        const val ACTION_RESUME = "com.countdownservicecomp.RESUME"
        private const val TAG = "CountdownTimer"
        const val EXTRA_TOTAL_MILLI = "totalMilli" // New Line
    }

    private var countDownTimer: CountDownTimer? = null
    private var timeRemaining: Long = 0L
    private var isPaused: Boolean = false
    private var timerActive: Boolean = false
    private var totalMilli: Double = 0.00
    private var confLimit: Int = 1
    private lateinit var sleepReceiver: SleepReceiver
    private val TRANSITIONS_RECEIVER_ACTION: String = "TRANSITIONS_RECEIVER_ACTION"

    override fun onCreate() {
        super.onCreate()

        sleepReceiver = SleepReceiver(
            {context, totalMilli -> startCountdown(context, totalMilli)},
            {pauseCountdown() },
            {resumeCountdown() },
            isPaused,
            timerActive,
            confLimit
        )

        val intentFilter = IntentFilter(TRANSITIONS_RECEIVER_ACTION)

        registerReceiver(sleepReceiver, intentFilter)

        val CHANNELID = "Foreground Service ID"
        val channel = NotificationChannel(
            CHANNELID,
            CHANNELID,
            NotificationManager.IMPORTANCE_LOW
        )

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification: Notification.Builder = Notification.Builder(this, CHANNELID)
            .setContentText("SnorLabs Running")
            .setContentTitle("SnorLabs")
            .setSmallIcon(R.drawable.snorlabs_icon_aug2023_512)

        startForeground(2, notification.build())

    }

    // Logic for timer here
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                //Start timer with the millis
                totalMilli = intent.getDoubleExtra(EXTRA_TOTAL_MILLI, 0.0) // New Line
                Log.d("CountdownService", "Total Milliseconds: $totalMilli")

                if (totalMilli > 0) {
                    startTracker()
                }
            }

            ACTION_PAUSE -> pauseCountdown()
            ACTION_RESUME -> resumeCountdown()
            else -> {
                Log.d(TAG, "No action specified.")
            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    // Asked prior to setting service in MainActivity
    private fun startTracker(){

        /*
        Activate sleep segment requests using pending intent to listen to activityRecognition API
        Broadcast receiver with Intent filter TRANSITIONS_RECEIVER_ACTION - this is linked to the
        sleepReceiver. When this intent is activated through the pendingIntent, it activates the
        sleep receiver.
         */

        val timerIntent = Intent(TRANSITIONS_RECEIVER_ACTION)
        timerIntent.putExtra(EXTRA_TOTAL_MILLI, totalMilli)

        val timerPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            timerIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val sleepSegmentRequest: SleepSegmentRequest? = null

        val task = ActivityRecognition.getClient(this).requestSleepSegmentUpdates(
            timerPendingIntent,
            SleepSegmentRequest.getDefaultSleepSegmentRequest()
        )

        Log.d("SleepService","StartTracking")

    }

    class SleepReceiver(
        private val startCountdown: (Context, Long) -> Unit,
        private val pauseCountdown: () -> Unit,
        private val resumeCountdown: () -> Unit,
        private var isPaused: Boolean,
        private var timerActive: Boolean,
        private var confLimit: Int
    ) : BroadcastReceiver() {
        //Broadcast receiver looking for activity recognition broadcasts

        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) return

            val totalMilli = intent.getDoubleExtra(EXTRA_TOTAL_MILLI, 0.0)

            // Initialising a list, API driven list with timestamp, sleep confidence,  device motion,
            // ambient light level.
            var sleepClassifyEvents: List<SleepClassifyEvent?>

            // Extract the required info from the activity recognition broadcast (intent)
            sleepClassifyEvents = SleepClassifyEvent.extractEvents(intent)

            Log.d(TAG, "SleepReceiver")
            Log.d(TAG, "SleepConf = $confLimit")
            Log.d(TAG, "isPaused = $isPaused")
            Log.d(TAG, "timerActive = $timerActive")

            if (SleepClassifyEvent.hasEvents(intent)) {
                // If the intent has the required sleepActivity info, this loop reviews the data.
                Log.d(TAG, "hasEvents True")

                val result = SleepClassifyEvent.extractEvents(intent)

                // Initialising an array to store sleepConfidence values
                val sleepConfidence = ArrayList<Int>()

                for (event in result) {
                    // Pulls out the sleepConfidence value from the SleepClassifyEventList
                    val confTimerInt = event.confidence

                    // Add the sleep confidence value to the sleepConfidence array.
                    sleepConfidence.add(event.confidence)
                    Log.d(TAG, "SleepConf Array: $sleepConfidence")

                    //LOOP 1: if there is no timer active (!timerActive), activate timer.
                    if (confTimerInt >= confLimit && !timerActive && !isPaused) {
                        Log.d(TAG, "Loop 1")
                        timerActive = true
                        startCountdown(context, totalMilli.toLong())

                    // LOOP 2: If confident User is now awake AFTER timer has started,
                    // pause the active timer.
                    } else if (confTimerInt < confLimit && timerActive && !isPaused) {
                        Log.d(TAG, "Loop 2")
                        isPaused = true
                        pauseCountdown()

                    // LOOP 3: If confider User is now asleep AFTER timer has started,
                    // but timer paused, resume the timer
                    // Confidence is high user is asleep, timer has already been started,
                    // but timer is not currently active
                    } else if (confTimerInt > confLimit && timerActive && isPaused) {
                        Log.d(TAG, "Loop 3")
                        isPaused = false
                        resumeCountdown()

                    // LOOP 4: If confident User is still awake after pause,
                    // the timer stays in a paused state
                    } else if (confTimerInt < confLimit && timerActive && isPaused) {
                        Log.d(TAG, "Loop 4")
                        pauseCountdown()
                    }
                }

                //confLimit = if (confLimit == 1) 100 else 1

            } else {
                Log.d(TAG, "hasEvents False")
            }
        }
    }


    private fun startCountdown(context: Context, millis: Long) {
        Log.d(TAG,"startCountdown")

        countDownTimer?.cancel()  // Cancel any existing countdown

        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    timeRemaining = millisUntilFinished
                    broadcastTimeRemaining(timeRemaining)
                    Log.d(TAG,"timeRemaining 1: $timeRemaining")
                }
            }

            override fun onFinish() {
                val alarmIntent = Intent(applicationContext, AlarmService::class.java).apply {
                    putExtra("NotificationMessage", "Test")
                }
                context.startService(alarmIntent)
                stopSelf()
            }
        }
        countDownTimer?.start()
    }

    private fun pauseCountdown(){
        Log.d(TAG, "pauseCountdown")
        countDownTimer?.cancel()
        //isPaused = true
        //Log.d(TAG,"isPaused, pauseCountdown $isPaused")
        broadcastPauseState(true)
    }

    private fun resumeCountdown(){
        Log.d(TAG, "resumeCountdown")
        //isPaused = false
        startCountdown(applicationContext, timeRemaining)
        broadcastPauseState(false)
    }

    private fun broadcastTimeRemaining(timeRemaining: Long) {
        Log.d(TAG,"Broadcast: On")
        val intent = Intent(COUNTDOWN_BR)

        var hms: String = String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(timeRemaining),
            TimeUnit.MILLISECONDS.toMinutes(timeRemaining) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeRemaining)),
            TimeUnit.MILLISECONDS.toSeconds(timeRemaining) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeRemaining))
        )

        intent.putExtra(EXTRA_TIME_REMAINING, hms) // Convert to seconds
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastPauseState(isPaused: Boolean) {
        val intent = Intent(COUNTDOWN_BR)
        intent.putExtra(EXTRA_PAUSE_STATE, isPaused)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        Log.d(TAG,"onDestroy Timer")
        countDownTimer?.cancel()  // Cancel the countdown when the service is destroyed

        // Unregister the receiver only if it has been initialized
        if (this::sleepReceiver.isInitialized) {
            unregisterReceiver(sleepReceiver)
        }

        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

}