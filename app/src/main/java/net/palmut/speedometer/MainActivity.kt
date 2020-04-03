package net.palmut.speedometer

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.security.SecureRandom
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        buttonA.setOnClickListener {
            Toast.makeText(this, R.string.button_a_text, Toast.LENGTH_SHORT).show()
        }
        buttonB.setOnClickListener {
            Toast.makeText(this, R.string.button_b_text, Toast.LENGTH_SHORT).show()
        }
        buttonC.setOnClickListener {
            Toast.makeText(this, R.string.button_c_text, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        hideSystemUI()
    }

    lateinit var job: Job

    override fun onResume() {
        super.onResume()
        job = GlobalScope.launch {
            var speed = 0
            var increment = 1
            while (isActive) {
                speedometer.value = speed
                speed += increment
                if (speed > 110) increment = -1
                if (speed < 0) increment = 1
                delay(5)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        job.cancel()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}