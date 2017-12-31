package com.prentice.ethan.smartmirror1.notifications

import android.app.Service
import android.content.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.IBinder
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.prentice.ethan.smartmirror1.Utilities


// Notification Receiver Service
class NRService : Service() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private val storageRef = FirebaseStorage.getInstance().reference

    private lateinit var nReceiver: NotificationReceiver

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        nReceiver = NotificationReceiver()
        val filter = IntentFilter()
        filter.addAction("com.prentice.ethan.smartmirror.NotificationListener")
        registerReceiver(nReceiver, filter)

        println("NRService Created")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(nReceiver)
    }

    internal inner class NotificationReceiver : BroadcastReceiver() {

        private var config: SharedPreferences = getSharedPreferences("config", 0)
        private var notifIcons: SharedPreferences = getSharedPreferences("notif_icons", 0)

        override fun onReceive(context: Context, intent: Intent) {

            val fileName = "notifs.json"

            if (config.getBoolean("NotifsEnabled", false)) {

                val map = intent.getSerializableExtra("notification_event") as HashMap<Notification.NotifValues, Any?>
                var uploadIcon = false

                // If notification has an icon, convert it to BitmapDrawable
                if (map[Notification.NotifValues.Icon] is Icon) {
                    val drawable = (map[Notification.NotifValues.Icon] as Icon).loadDrawable(this@NRService)

                    // If the icon has already been uploaded to Firebase, don't upload it again.
                    if ((map[Notification.NotifValues.Icon] as Icon).toString() !in notifIcons) {
                        val editor = notifIcons.edit()
                        editor.putBoolean((map[Notification.NotifValues.Icon] as Icon).toString(), true)
                        editor.apply()
                        uploadIcon = true
                    }
                    map[Notification.NotifValues.Icon] = drawable
                }

                val notif = Notification(map)

                // Do not handle notification if it is junk
                if (!notif.junk) {

                    val notifJson = notif.convertToJSON()
                    var parsedJson = Utilities.parseJSON(this@NRService, fileName)

                    // If notification was removed, remove all similar notifications from parsedJson
                    // If notification was posted, add notification to parsedJson
                    if (notif.map[Notification.NotifValues.Status] == "Removed") {
                        if (parsedJson != null) {
                            parsedJson = notif.removeNotif(parsedJson)
                        }
                    } else {
                        parsedJson = putNotif(parsedJson, notifJson)
                    }

                    // Upload Json to Firebase and update Json on disk (internal)
                    if (parsedJson != null) {
                        val fileString = parsedJson.toJsonString(prettyPrint = true)
                        Utilities.uploadStream(storageRef, fileString, "user_data/${currentUser.uid}", fileName)

                        Utilities.writeToFile(this@NRService, fileName, fileString)
                    }

                    // TODO: Develop a way to give each icon a unique identifier so there can be multiple icons per application
                    // If icon is not already in Firebase according to local logs, upload to Firebase
                    if (uploadIcon) {
                        val notifFileName = notif.map[Notification.NotifValues.PackageName]
                        val bitmapDrawable = (notif.map[Notification.NotifValues.Icon]!!) as BitmapDrawable

                        Utilities.uploadStream(storageRef, bitmapDrawable, "icons", "$notifFileName.png")
                    }
                }

            }
        }


        private fun putNotif(parsedJson: JsonObject?, notifJson: JsonObject): JsonObject {
            // If the JSON is not null, just add the notification to the JsonArray
            // If the JSON is null, create a JSON, then add the notification to it's JsonArray
            if (parsedJson != null) {
                (parsedJson["Notifications"] as JsonArray<JsonObject>).add(notifJson)

                return parsedJson
            } else {
                val emptyMap = HashMap<String, Any?>()
                emptyMap.put("Notifications", JsonArray<JsonObject>())
                val emptyJson = JsonObject(emptyMap)
                (emptyJson["Notifications"] as JsonArray<JsonObject>).add(notifJson)

                return emptyJson
            }
        }
    }

}
