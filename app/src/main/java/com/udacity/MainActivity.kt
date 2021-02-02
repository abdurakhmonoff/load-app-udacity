package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var downloadManager: DownloadManager
    private var downloadID: Long = 0

    private var glideDownloadId = 0L
    private var repositoryDownloadId = 0L
    private var retrofitDownloadId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        createChannel(getString(R.string.notification_downloaded_channel_id), getString(R.string.notification_title))

        custom_button.setOnClickListener {
            if (download_file_group.checkedRadioButtonId!=-1) custom_button.setLoadingState(ButtonState.Clicked)
            when (download_file_group.checkedRadioButtonId) {
                R.id.download_file_1 -> download(GLIDE_URL)
                R.id.download_file_2 -> download(LOAD_APP_URL)
                R.id.download_file_3 -> download(RETROFIT_URL)
                else -> Toast.makeText(this, "Please select the file to download", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id!!))
            if (cursor.moveToNext()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                val notificationManager = ContextCompat.getSystemService(context!!, NotificationManager::class.java)
                cursor.close()
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        notificationManager?.sendNotification(context.getString(R.string.notification_description), context, context.getString(when (id) {
                            glideDownloadId -> R.string.glide_image_loading_library
                            repositoryDownloadId -> R.string.loadapp_current_repository
                            else -> R.string.retrofit_type_face_http_client
                        }), "Failed")
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        notificationManager?.sendNotification(context.getString(R.string.notification_description), context, context.getString(when (id) {
                            glideDownloadId -> R.string.glide_image_loading_library
                            repositoryDownloadId -> R.string.loadapp_current_repository
                            else -> R.string.retrofit_type_face_http_client
                        }), "Success")
                    }
                }
            }
        }
    }

    private fun download(url: String) {
        val request =
                DownloadManager.Request(Uri.parse(url))
                        .setTitle(getString(R.string.app_name))
                        .setDescription(getString(R.string.app_description))
                        .setRequiresCharging(false)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)

        when (url) {
            // enqueue puts the download request in the queue.
            GLIDE_URL -> glideDownloadId = downloadManager.enqueue(request)
            LOAD_APP_URL -> repositoryDownloadId = downloadManager.enqueue(request)
            RETROFIT_URL -> retrofitDownloadId = downloadManager.enqueue(request)
        }
    }

    companion object {
        private const val GLIDE_URL = "https://github.com/bumptech/glide/archive/master.zip"
        private const val LOAD_APP_URL = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val RETROFIT_URL = "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_description)

            val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

}
