package com.cristiangarrido.turnonscreenfromservice

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import kotlinx.android.parcel.Parcelize
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import timber.log.Timber


/**
 * Created by cristian on 15/01/18.
 */
class EventService : IntentService("MyService") {

    companion object {
        private const val CHANNEL_ID = "EventService Channel"
        private const val COMMAND_EXTRA = "COMMAND_EXTRA"

        fun startIntent(context: Context, command: Command): Intent {
            return Intent(context, EventService::class.java).also {
                it.putExtra(COMMAND_EXTRA, command)
            }
        }

    }

    private val notificationId = 1
    private val preBuiltNotification: NotificationCompat.Builder
        get() = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("An event !")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(longArrayOf(0, 250, 250, 250))

    private val handler = Handler()

    private val notificationManager: NotificationManager
            by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {
        intent?.let {

            startForeground(notificationId, preBuiltNotification.build())
            val command = it.getParcelableExtra<Command>(COMMAND_EXTRA)
            Timber.i("Received command: $command")
            val mainActivityIntent = PendingIntent.getActivity(baseContext, 0,
                    MainActivity.updateIntent(baseContext, "Coucou!"), 0)

            val notification = preBuiltNotification
                    .setContentInfo(command.action)
                    .setContentIntent(mainActivityIntent)
                    .build()

            handler.postDelayed({
                turnScreenOn()
                broadcastUnlockScreen()
                notificationManager.notify(notificationId, notification)

            }, 3000)
            stopForeground(true)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.w("Service destroyed!")
    }

    fun broadcastUnlockScreen() {
        sendBroadcast(Intent("unlockScreen"))
    }

    @Suppress("DEPRECATION")
    fun turnScreenOn() {
        Timber.d("turn on screen !")
        val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isAlreadyOn = /*Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH && */powerManager.isInteractive
                || /*Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH && */powerManager.isScreenOn

        if (!isAlreadyOn) {
            val wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "MH24_SCREENLOCK")
            wl.acquire(10000)
            val wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MH24_SCREENLOCK")
            wl_cpu.acquire(10000)
        }
    }


    @SuppressLint("ParcelCreator")
    @Parcelize
    data class Command(val action: String) : Parcelable
}