package com.prentice.ethan.smartmirror1.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.IntentFilter
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import com.prentice.ethan.smartmirror1.notifications.Notification.NotifValues


/**
 * Created by Ethan on 2017-12-28.
 */

// Notification Listener Service
class NLService : NotificationListenerService() {

    private val nlServiceReceiver = NLServiceReceiver()

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter()
        filter.addAction("com.prentice.ethan.smartmirror.NotificationListenerService")
        registerReceiver(nlServiceReceiver, filter)

        println("NLService Created")
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(nlServiceReceiver)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        /*
        Log.i(TAG, "onNotificationPosted")
        Log.i(TAG, "ID :" + sbn.id + "\t" + sbn.notification.tickerText + "\t" + sbn.packageName)
        */

        val notifMap = HashMap<NotifValues, Any?>().apply {
            put(NotifValues.Icon, sbn.notification.smallIcon)
            put(NotifValues.Status, "Posted")
            put(NotifValues.PackageName, sbn.packageName)
            put(NotifValues.Message, sbn.notification.tickerText)
            put(NotifValues.TimeReceived, sbn.postTime)
            put(NotifValues.TimeRemoved, null)
        }

        val i = Intent("com.prentice.ethan.smartmirror.NotificationListener")
        i.putExtra("notification_event", notifMap)

        sendBroadcast(i)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

        /*
        Log.i(TAG, "onNotificationPosted")
        Log.i(TAG, "ID :" + sbn.id + "\t" + sbn.notification.tickerText + "\t" + sbn.packageName)
        */

        val notifMap = HashMap<NotifValues, Any?>().apply {
            put(NotifValues.Icon, sbn.notification.smallIcon)
            put(NotifValues.Status, "Removed")
            put(NotifValues.PackageName, sbn.packageName)
            put(NotifValues.Message, sbn.notification.tickerText)
            put(NotifValues.TimeReceived, sbn.postTime)
            put(NotifValues.TimeRemoved, System.currentTimeMillis())
        }

        val i = Intent("com.prentice.ethan.smartmirror.NotificationListener")
        i.putExtra("notification_event", notifMap)

        sendBroadcast(i)
    }


    internal inner class NLServiceReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getStringExtra("command") == "clearall") {

                this@NLService.cancelAllNotifications()

            } else if (intent.getStringExtra("command") == "list") {

                val i1 = Intent("com.prentice.ethan.smartmirror.NotificationListener")
                i1.putExtra("notification_event", "=====================")
                sendBroadcast(i1)

                var i = 1
                for (sbn in this@NLService.activeNotifications) {
                    val i2 = Intent("com.prentice.ethan.smartmirror.NotificationListener")
                    i2.putExtra("notification_event", i.toString() + " " + sbn.packageName + "\n")
                    sendBroadcast(i2)
                    i++
                }

                val i3 = Intent("com.prentice.ethan.smartmirror.NotificationListener")
                i3.putExtra("notification_event", "===== Notification List ====")
                sendBroadcast(i3)
            }
        }
    }
}
