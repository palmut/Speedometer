package net.palmut.speedometer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.*
import android.view.ViewGroup.LayoutParams.*
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import net.palmut.aidlservice.ITestAidlInterface

class MainActivity : AppCompatActivity() {

    private var dataProvider: ITestAidlInterface? = null
    private var readData: Job? = null
    private lateinit var speedometer: SpeedometerView
    private lateinit var tachometer: TachometerView

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected")
            stopReceiveData()
            dataProvider = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected")
            dataProvider = ITestAidlInterface.Stub.asInterface(service)
            startReceiveData()
        }

    }

    private val pagerAdapter = object : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return when (viewType) {
                PAGE_SPEEDOMETER -> ViewHolder(speedometer)
                PAGE_TACHOMETER -> ViewHolder(tachometer)
                else -> ViewHolder(View(parent.context).apply {
                    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
                })
            }
        }

        override fun getItemCount() = PAGES_COUNT

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // do nothing
        }

        override fun getItemViewType(position: Int) = position
    }

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

        val lp = MarginLayoutParams(MATCH_PARENT, MATCH_PARENT)
        speedometer = SpeedometerView(this).apply { layoutParams = lp }
        tachometer = TachometerView(this).apply { layoutParams = lp }

        pager.adapter = pagerAdapter
        pager.setPageTransformer { page, position ->
            if (position < 0) {
                page.alpha = 1 + position
                page.translationX = -position * page.width / 2
            }
        }
        pager.isUserInputEnabled = false
    }

    override fun onStart() {
        super.onStart()

        try {
            val intent = Intent().setComponent(ComponentName("net.palmut.aidlservice", "net.palmut.aidlservice.TestAIDLService"))
                    .setAction("net.palmut.speedomenter.DATA")
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
        }
    }

    override fun onStop() {
        super.onStop()
        if (dataProvider != null) {
            unbindService(connection)
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

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    private fun startReceiveData() {
        readData = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive && dataProvider != null) {
                dataProvider?.run {
                    withContext(Dispatchers.Main) {
                        try {
                            speedometer.value = speed().toFloat()
                            tachometer.value = rpm().toFloat() / 1000
                        } catch (e: java.lang.Exception) {
                            Log.d(TAG, Log.getStackTraceString(e))
                        }
                    }
                    delay(57)
                }
            }
        }
    }

    private fun stopReceiveData() {
        readData?.cancel()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PAGES_COUNT = 2
        private const val PAGE_SPEEDOMETER = 0
        private const val PAGE_TACHOMETER = 1
    }

    private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}