package net.palmut.aidlservice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlin.math.sin

class TestAIDLService : Service() {

    private val binder = object : ITestAidlInterface.Stub() {

        val value: Float get() = (System.currentTimeMillis() % 100000) / 1000f

        override fun rpm(): Int {
            return MAX_RPM / 2 + (MAX_RPM / 2 * sin(value) + MAX_RPM / 4 * sin(2 * value) + MAX_RPM / 8 * sin(4 * value) + MAX_RPM / 16 * sin(
                8 * value)).toInt()
        }

        override fun speed(): Int {
            return MAX_SPEED / 2 + (MAX_SPEED / 2 * sin(value) + MAX_SPEED / 30 * sin(20 * value) + MAX_SPEED / 41 * sin(23 * value)).toInt()
        }

    }

    override fun onBind(intent: Intent?): IBinder? = binder

    companion object {
        private const val TAG = "TestAIDLService"
        private const val MAX_SPEED = 120
        private const val MAX_RPM = 9000
    }
}