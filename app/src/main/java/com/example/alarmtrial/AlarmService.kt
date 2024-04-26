package com.example.alarmtrial

import android.app.Service
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