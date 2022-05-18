package im.vector.app.kelare.content


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class LockStateReceiver(private val mContext: Context) {
    private val receiver: ScreenBroadcastReceiver?
    private var mScreenStateListener: ScreenStateListener? = null

    init {
        receiver = ScreenBroadcastReceiver()
    }

    fun register(screenStateListener: ScreenStateListener?) {
        if (screenStateListener != null) {
            mScreenStateListener = screenStateListener
        }
        if (receiver != null) {
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            filter.addAction(Intent.ACTION_SCREEN_ON)
            filter.addAction(Intent.ACTION_USER_PRESENT)
            mContext.registerReceiver(receiver, filter)
        }
    }

    fun unregister() {
        if (receiver != null) {
            mContext.unregisterReceiver(receiver)
        }
    }


    private inner class ScreenBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                val action = intent.action
                if (Intent.ACTION_SCREEN_ON == action) {
                    if (mScreenStateListener != null) {
                        mScreenStateListener!!.onScreenOn()
                    }
                } else if (Intent.ACTION_SCREEN_OFF == action) {
                    if (mScreenStateListener != null) {
                        mScreenStateListener!!.onScreenOff()
                    }
                } else if (Intent.ACTION_USER_PRESENT == action) {
                    if (mScreenStateListener != null) {
                        mScreenStateListener!!.onUserPresent()
                    }
                }
            }
        }
    }

    interface ScreenStateListener {
        fun onScreenOn()

        fun onScreenOff()

        fun onUserPresent()
    }
}

