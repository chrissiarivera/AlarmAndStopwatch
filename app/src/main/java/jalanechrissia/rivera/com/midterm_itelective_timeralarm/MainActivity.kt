package jalanechrissia.rivera.com.midterm_itelective_timeralarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var mBtnStart: ImageView
    lateinit var mBtnPause: ImageView
    lateinit var mBtnReset: ImageView
    lateinit var mBtnLap: ImageView
    lateinit var mPlay: TextView
    lateinit var mPause: TextView
    lateinit var mReset: TextView
    lateinit var mLap: TextView
    lateinit var mTimeContainer: LinearLayout
    lateinit var inflater: LayoutInflater
    lateinit var txtTimeView: TextView
    lateinit var addView: View
    lateinit var mHandler: Handler
    lateinit var mTimeView: TextView
    lateinit var alMan: AlarmManager
    lateinit var tiPi: TimePicker
    lateinit var updateText: TextView
    lateinit var con: Context
    lateinit var btnStart: Button
    lateinit var btnStop: Button
    lateinit var penInt: PendingIntent
    var hour: Int = 0
    var min: Int = 0
    var sec: Int = 0
    var startTime: Long = 0L
    var timeBuff: Long = 0L
    var updateTime: Long = 0L
    var milliSecondTime: Long = 0L
    var millisec: Int = 0

    private val runnable = object : Runnable {
        override fun run() {
            milliSecondTime = SystemClock.uptimeMillis() - startTime
            updateTime = timeBuff + milliSecondTime
            sec = (updateTime / 1000).toInt()
            min = sec / 60
            sec %= 60
            millisec = (updateTime % 1000).toInt()
            mTimeView.text = ("" + min + ":"
                    + String.format("%02d", sec) + ":"
                    + String.format("%2d", millisec))
            mHandler.postDelayed(this, 0)
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViews()

        //Setting up of tab
        tabHost.setup()

        var spec = tabHost.newTabSpec("Alarm Tab")
        spec.setContent(R.id.alarm)
        spec.setIndicator("Alarm Tab")
        tabHost.addTab(spec)

        spec = tabHost.newTabSpec("Stopwatch Tab")
        spec.setContent(R.id.stopwatch)
        spec.setIndicator("Stopwatch Tab")
        tabHost.addTab(spec)

        //Alarm
        this.con = this
        alMan = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        tiPi = findViewById(R.id.timePicker) as TimePicker
        updateText = findViewById(R.id.updateText) as TextView
        btnStart = findViewById(R.id.start_alarm) as Button
        btnStop = findViewById(R.id.stop_alarm) as Button
        var calendar: Calendar = Calendar.getInstance()
        var mIntent: Intent = Intent(this, AlarmReceiver::class.java)
        btnStart.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    calendar.set(Calendar.HOUR_OF_DAY, tiPi.hour)
                    calendar.set(Calendar.MINUTE, tiPi.minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    hour = tiPi.hour
                    min = tiPi.minute
                }else{
                    calendar.set(Calendar.HOUR_OF_DAY, tiPi.currentHour)
                    calendar.set(Calendar.MINUTE, tiPi.currentMinute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    hour = tiPi.currentHour
                    min = tiPi.currentMinute
                }
                var hour_string: String = hour.toString()
                var min_string: String = min.toString()
                if (hour > 12){
                    hour_string = (hour - 12).toString()
                }
                if (min < 10){
                    min_string = "0$min"
                }

                setAlarmText("Alarm set to: $hour_string : $min_string")
                mIntent.putExtra("extra", "on")
                penInt = PendingIntent.getBroadcast(this@MainActivity, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                alMan.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, penInt)
            }
        })

        btnStop.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                setAlarmText("Alarm off")
                penInt = PendingIntent.getBroadcast(this@MainActivity, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                alMan.cancel(penInt)
                mIntent.putExtra("extra", "off")
                sendBroadcast(mIntent)
            }

        })

        //Stopwatch
        mHandler = Handler()
        setDisableButtonORsetColor()
        buttonOnClicks()

    }

    private fun setAlarmText(s: String) {
        updateText.setText(s)
    }

    private fun buttonOnClicks() {
        mBtnStart.setOnClickListener {
            startTime()
        }
        mBtnPause.setOnClickListener {
            pauseTime()
        }
        mBtnReset.setOnClickListener {
            resetTime()
        }
        mBtnLap.setOnClickListener {
            inflater = baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            addView = inflater.inflate(R.layout.time_list, null)
            txtTimeView = addView.findViewById(R.id.textContainer)
            txtTimeView.text = mTimeView.text.toString()
            mTimeContainer.addView(addView)
        }
    }

    private fun resetTime() {
        milliSecondTime = 0L
        startTime = 0L
        timeBuff = 0L
        updateTime = 0L
        sec = 0
        min = 0
        millisec = 0
        mTimeView.text = "00:00:00"
        mTimeContainer.removeAllViews()
        mBtnReset.isEnabled = false
        mBtnReset.setColorFilter(Color.GRAY)
        mReset.setTextColor(Color.GRAY)
    }

    private fun pauseTime() {
        timeBuff += milliSecondTime
        mHandler.removeCallbacks(runnable)
        mBtnPause.visibility = View.INVISIBLE
        mBtnStart.visibility = View.VISIBLE
        mBtnReset.isEnabled = true
        mBtnReset.setColorFilter(Color.BLACK)
        mReset.setTextColor(Color.BLACK)
        mBtnLap.isEnabled = false
        mBtnLap.setColorFilter(Color.GRAY)
        mLap.setTextColor(Color.GRAY)
        mPause.visibility = View.INVISIBLE
        mPlay.visibility = View.VISIBLE
    }

    private fun startTime() {
        startTime = SystemClock.uptimeMillis()
        mHandler.postDelayed(runnable, 0)
        mBtnStart.visibility = View.INVISIBLE
        mBtnPause.visibility = View.VISIBLE
        mBtnLap.isEnabled = true
        mBtnLap.setColorFilter(Color.BLACK)
        mLap.setTextColor(Color.BLACK)
        mPause.visibility = View.VISIBLE
        mPause.visibility = View.VISIBLE
        mPlay.visibility = View.INVISIBLE
        mBtnReset.isEnabled = false
        mBtnReset.setColorFilter(Color.GRAY)
        mReset.setTextColor(Color.GRAY)
    }

    private fun setDisableButtonORsetColor() {
        mBtnReset.isEnabled = false
        mBtnReset.setColorFilter(Color.GRAY)
        mReset.setTextColor(Color.GRAY)
        mBtnLap.isEnabled = false
        mBtnLap.setColorFilter(Color.GRAY)
        mLap.setTextColor(Color.GRAY)
    }

    private fun findViews() {
        mBtnStart = findViewById(R.id.btnStart)
        mBtnPause = findViewById(R.id.btnPause)
        mBtnReset = findViewById(R.id.btnReset)
        mBtnLap = findViewById(R.id.btnLap)
        mPlay = findViewById(R.id.txtPlay)
        mPause = findViewById(R.id.txtPause)
        mReset = findViewById(R.id.txtReset)
        mLap = findViewById(R.id.txtLap)
        mTimeView = findViewById(R.id.tvTimer)
        mTimeContainer = findViewById(R.id.tvTimeContainer)
    }

}
