package com.snorlabs.alarmtrial

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

class AlarmService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var vibrator: Vibrator

    private lateinit var alarmNotification: AlarmNotification

    override fun onCreate() {
        super.onCreate()

        alarmNotification = AlarmNotification(this)

        mediaPlayer = MediaPlayer.create(this,R.raw.bell)

        mediaPlayer.isLooping = true

        vibrator = getSystemService(Vibrator::class.java)

        Log.d(TAG,"onCreate")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(TAG,"onStart")

        alarmNotification.createNotification()

        mediaPlayer.start()

        vibrate()

        // Cancels the pending intent of regular alarm if dynamic goes off
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create pending intent with same name and requestCode to cancel.
        val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("NotificationMessage", "Test")
        }

        val alarmPendingintent = PendingIntent
            .getBroadcast(
                this,
                0,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.cancel(alarmPendingintent)

        // Cancels the Countdown Service if regular alarm goes off
        // Stop the countdown service
        val intent = Intent(this, CountdownTimer::class.java)
        this.stopService(intent)

        //Close Notification
        val notificationManager =
            this?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(3)

        //Opens up the cancel alarm activity for user. Service remains until closed.
        val activityIntent = Intent(this
            , AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

        this.startActivity(activityIntent)

        return START_STICKY

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"onDestroy")

        mediaPlayer.stop()

        mediaPlayer.release()

        vibrator.cancel()
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
        return null
    }

    private fun vibrate(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000, 500, 1000), 0))
        } else {
            // Deprecated in API 26
            vibrator.vibrate(longArrayOf(0, 1000, 500, 1000), 0)
        }
    }

    companion object {
        private const val TAG = "AlarmService"
    }

}