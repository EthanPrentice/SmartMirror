package com.prentice.ethan.smartmirror1

import android.content.Context
import android.os.AsyncTask
import com.google.firebase.storage.StorageReference
import android.util.Log
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.*
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable


/**
 * Created by Ethan on 2017-12-29.
 */


class Utilities {
    companion object {

        private class StreamUploader: AsyncTask<Any, Boolean, Boolean>() {
            override fun doInBackground(vararg args: Any): Boolean {
                if (args[0] is StorageReference && args[1] is String && args[3] is String) {
                    val storageRef = args[0] as StorageReference
                    val path = args[1] as String
                    val fileName = args[3] as String
                    val fileRef = storageRef.child(path).child(fileName)
                    val obj = args[2]
                    val objBytes: ByteArray

                    // If the object is a BitmapDrawable, compress into a PNG and upload to Firebase
                    // Otherwise, upload object as a string
                    if (obj is BitmapDrawable) {
                        val bitmap = obj.bitmap
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        objBytes = stream.toByteArray()

                    } else {
                        objBytes = obj.toString().toByteArray()
                    }
                    fileRef.putStream(objBytes.inputStream())

                    return true

                } else {
                    return false
                }
            }
        }

        fun uploadStream(storageRef: StorageReference, obj: Any, path: String, fileName: String) {
            StreamUploader().execute(storageRef, path, obj, fileName)
        }

        fun downloadFile(storageRef: StorageReference, path: String, fileName: String): Boolean {
            val localFile = File(fileName)
            val fileRef = storageRef.child(path).child(fileName)
            var success = false

            fileRef.getFile(localFile).addOnSuccessListener( {
                Log.i("FileDownloader", "Success downloading $fileName")
                success = true
            }).addOnFailureListener( {
                Log.i("FileDownloader", "There was an error downloading $fileName")
            })

            return success
        }


        fun parseJSON(context: Context, fileName: String) : JsonObject? {
            try {
                val fileContents = readFromFile(context, fileName)

                val parser = Parser()
                val stringBuilder = StringBuilder(fileContents)

                if (!stringBuilder.isEmpty()) {
                    return parser.parse(stringBuilder) as JsonObject?
                } else {
                    return parser.parse("{}") as JsonObject?
                }

            } catch (e: IOException) {
                return null
            }
        }

        fun writeToFile(context: Context, fileName: String, data: String) {
            try {
                val outputStreamWriter = OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE))
                outputStreamWriter.write(data)
                outputStreamWriter.close()
            } catch (e: IOException) {
                Log.e("Exception", "File write failed: " + e.toString())
            }
        }

        fun readFromFile(context: Context, fileName: String): String? {
            try {
                val inputStream = context.openFileInput(fileName)
                if (inputStream != null) {
                    val inputStreamReader = InputStreamReader(inputStream)
                    val bufferedReader = BufferedReader(inputStreamReader)
                    var fileString = ""

                    println("LINES:")
                    val lines = bufferedReader.readLines()

                    for (line in lines) {
                        fileString += line
                    }

                    return fileString
                }
            } catch (e: FileNotFoundException) {
                Log.e("Exception", "File not found: " + e.toString())
            } catch (e: IOException) {
                Log.e("Exception", "Can not read file: " + e.toString())
            }
            return null
        }

    }
}