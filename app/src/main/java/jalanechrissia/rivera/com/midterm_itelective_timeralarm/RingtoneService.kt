package jalanechrissia.rivera.com.midterm_itelective_timeralarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import android.support.v4.app.NotificationCompat

/**
 * Created by Jalane Chrissia on 07/02/2018.
 */
class RingtoneService : Service() {

    companion object {
        lateinit var r: Ringtone
    }
    var id: Int = 0
    var isRunning: Boolean = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var state: String = intent!!.getStringExtra("extra")
        assert(state != null)
        when(state){
            "on" -> id = 1
            "off" -> id = 0
        }
        if (!this.isRunning && id == 1){
            playAlarm()
            this.isRunning = true
            this.id = 0
            fireNotification()
        }
        else if (this.isRunning && id == 0){
            r.stop()
            this.isRunning = false
            this.id = 0
        }
        else if (!this.isRunning && id == 0){
            this.isRunning = false
            this.id = 0
        }
        else if (this.isRunning && id == 1){
            this.isRunning = true
            this.id = 1
        }
        else{

        }

        return START_NOT_STICKY
    }

    private fun fireNotification() {
        var mainActivityIntent: Intent = Intent(this, MainActivity::class.java)
        var penInt: PendingIntent = PendingIntent.getActivity(this, 0 , mainActivityIntent, 0)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var notifyManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var notification = NotificationCompat.Builder(this)
                .setContentTitle("Alarm is going off")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(defaultSoundUri)
                .setContentText("Click me")
                .setContentIntent(penInt)
                .setAutoCancel(true)
                .build()
//        var notification: NotificationCompat.Builder = NotificationCompat.Builder(this, "notify")
//        notification.setContentTitle("Alarm is going off")
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setSound(defaultSoundUri)
//                .setContentText("Click me")
//                .setContentIntent(penInt)
//                .setAutoCancel(true)

        notifyManager.notify(0, notification)
//        notifyManager.notify(0, notification.build())
    }

    private fun playAlarm() {
        var alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmUri == null){
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        r = RingtoneManager.getRingtone(baseContext, alarmUri)
        r.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.isRunning = false
    }

}