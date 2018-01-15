package com.cristiangarrido.turnonscreenfromservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    companion object {
        private const val MESSAGE_EXTRA = "MESSAGE_EXTRA"

        fun updateIntent(context: Context, message: String): Intent {
            return Intent(context, MainActivity::class.java).also {
                it.putExtra(MESSAGE_EXTRA, message)
            }
        }
    }

    fun unlockScreen() {
        Timber.d("I'm awake !")
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }

    private val unlockScreenReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            unlockScreen()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            startService(EventService.startIntent(baseContext, EventService.Command("Hola guapeton!")))
        }

        registerReceiver(unlockScreenReceiver, IntentFilter("unlockScreen"))
    }

    override fun onDestroy() {
        unregisterReceiver(unlockScreenReceiver)
        super.onDestroy()
    }
}
