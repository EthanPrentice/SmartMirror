package com.prentice.ethan.smartmirror1.notifications

import android.util.Log
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject

/**
 * Created by Ethan on 2017-12-28.
 */

class Notification (notifMap: HashMap<NotifValues, Any?>) {

    val TAG = "Notifications"

    enum class NotifValues {
        Icon,
        Status,
        PackageName,
        Message,
        TimeReceived,
        TimeRemoved,
        Junk
    }

    var map = HashMap<NotifValues, Any?>()
    var junk = false

    init {
        junk = isJunk(notifMap)
        notifMap.put(NotifValues.Junk, junk)

        if (!(notifMap[NotifValues.Junk] as Boolean)) {
            if (notifMap[NotifValues.Status] == "Posted") {

                Log.d(TAG, "NEW NOTIFICATION: ")
                notifMap.forEach {
                    Log.d(TAG, "${it.key.name} \t:\t${it.value}")
                }

            }

        }

        this.map = notifMap
    }

    fun removeNotif(parsedJson: JsonObject): JsonObject {
        val notifs = parsedJson["Notifications"] as JsonArray<JsonObject>

        // Remove all filtered notifications
        notifs.filter {
            it["packageName"] == map[NotifValues.PackageName] &&
            it["message"] == map[NotifValues.Message] &&
            it["timeReceived"] == map[NotifValues.TimeReceived]
        }.forEach {
            notifs.remove(it)
        }

        parsedJson.remove("Notifications")
        parsedJson.put("Notifications", notifs)

        return parsedJson
    }

    // TODO: When method to give icons a unique ID is implemented change iconID from 0 to the unique ID
    fun convertToJSON(): JsonObject {
        val json = JsonObject()

        infix fun <T> String.hasValue(value: T) {
            json.put(this, value)
        }

        "status"        hasValue    map[NotifValues.Status]
        "packageName"   hasValue    map[NotifValues.PackageName]
        "iconID"        hasValue    0
        "message"       hasValue    map[NotifValues.Message]
        "timeReceived"  hasValue    map[NotifValues.TimeReceived]
        "timeRemoved"   hasValue    map[NotifValues.TimeRemoved]

        return json
    }

    // TODO: Allow the user to choose junk filters by application instead of being hardcoded
    private fun isJunk(notifMap: HashMap<NotifValues, Any?>): Boolean {
        if (notifMap[NotifValues.Message] == "" || notifMap[NotifValues.Message] == null) {
            return true
        }
        if (notifMap[NotifValues.PackageName] == "android") {
            return true
        }
        if (notifMap[NotifValues.PackageName] == "com.google.android.music") {
            return true
        }

        return false
    }

}
