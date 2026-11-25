package com.iustinmoraru.prtracker.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.data.settings.SettingsRepository
import com.iustinmoraru.prtracker.data.workout.Exercise
import com.iustinmoraru.prtracker.ui.auth.LoginActivity
import com.iustinmoraru.prtracker.ui.common.Format

/** Abstraction used by view models, so they do not depend on Android notification APIs. */
interface RecordNotifier {
    fun notifyNewRecords(exercises: List<Exercise>)
}

/**
 * Posts the app's system notifications: new personal records and the end of the deadhang timer.
 * Nothing is shown when the user disabled notifications in Settings or denied the permission.
 */
class AppNotifier(
    private val context: Context,
    private val settings: SettingsRepository,
) : RecordNotifier {

    /** Creates the notification channels (required on Android 8+). Safe to call repeatedly. */
    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    CHANNEL_RECORDS,
                    context.getString(R.string.channel_records),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = context.getString(R.string.channel_records_desc) },
                NotificationChannel(
                    CHANNEL_TIMER,
                    context.getString(R.string.channel_timer),
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply { description = context.getString(R.string.channel_timer_desc) },
            )
        )
    }

    override fun notifyNewRecords(exercises: List<Exercise>) {
        if (exercises.isEmpty()) return
        val lines = exercises.joinToString("\n") { exercise ->
            context.getString(R.string.notification_record_line, exercise.name, Format.weight(exercise.weightKg), exercise.reps)
        }
        post(
            id = NOTIFICATION_RECORD,
            channel = CHANNEL_RECORDS,
            title = context.getString(R.string.notification_record_title),
            text = lines,
        )
    }

    fun notifyTimerFinished(seconds: Int) {
        post(
            id = NOTIFICATION_TIMER,
            channel = CHANNEL_TIMER,
            title = context.getString(R.string.notification_timer_title),
            text = context.getString(R.string.notification_timer_text, seconds),
        )
    }

    fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // checked by hasPermission() above
    private fun post(id: Int, channel: String, title: String, text: String) {
        if (!settings.notificationsEnabled.value || !hasPermission()) return
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        // Opens the app (LoginActivity forwards signed-in users to the main screen).
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        try {
            manager.notify(id, notification)
        } catch (_: SecurityException) {
            // Permission revoked between the check and the call: nothing to show.
        }
    }

    private companion object {
        const val CHANNEL_RECORDS = "records"
        const val CHANNEL_TIMER = "timer"
        const val NOTIFICATION_RECORD = 1
        const val NOTIFICATION_TIMER = 2
    }
}
