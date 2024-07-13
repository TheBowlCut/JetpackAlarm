package com.example.alarmtrial

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

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
    }

    private var countDownTimer: CountDownTimer? = null
    private var timeRemaining: Long = 0L
    private var isPaused: Boolean = false

    // Logic for timer here
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                //Start timer with the millis
                val totalMilli = intent?.getDoubleExtra("totalMilli", 0.0) ?: 0.0
                Log.d("CountdownService", "Total Milliseconds: $totalMilli")

                if (totalMilli > 0){
                    startCountdown(totalMilli.toLong())
                }
            }
            ACTION_PAUSE -> pauseCountdown()
            ACTION_RESUME -> resumeCountdown()
            else -> {
                Log.d(TAG, "No action specified.")
            }
        }
        return START_NOT_STICKY
    }

    private fun startCountdown(millis: Long) {
        countDownTimer?.cancel()  // Cancel any existing countdown

        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    timeRemaining = millisUntilFinished
                    broadcastTimeRemaining(timeRemaining)
                    Log.d(TAG,"timeRemaining: $timeRemaining")
                }
            }

            override fun onFinish() {
                val alarmIntent = Intent(applicationContext, AlarmService::class.java).apply {
                    putExtra("NotificationMessage", "Test")
                }
                applicationContext.startService(alarmIntent)
                stopSelf()
            }
        }
        countDownTimer?.start()
    }

    private fun pauseCountdown(){
        countDownTimer?.cancel()
        isPaused = true
        broadcastPauseState(true)
    }

    private fun resumeCountdown(){
        isPaused = false
        startCountdown(timeRemaining)
        broadcastPauseState(false)
    }

    private fun broadcastTimeRemaining(timeRemaining: Long) {
        Log.d(TAG,"Broadcast: On")
        val intent = Intent(COUNTDOWN_BR)
        intent.putExtra(EXTRA_TIME_REMAINING, timeRemaining / 1000) // Convert to seconds
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastPauseState(isPaused: Boolean) {
        val intent = Intent(COUNTDOWN_BR)
        intent.putExtra(EXTRA_PAUSE_STATE, isPaused)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        countDownTimer?.cancel()  // Cancel the countdown when the service is destroyed
        super.onDestroy()
    }



    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }


}