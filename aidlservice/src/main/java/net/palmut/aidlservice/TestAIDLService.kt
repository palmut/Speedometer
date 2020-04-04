package net.palmut.aidlservice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlin.math.sin

class TestAIDLService : Service() {

    private val binder = object : ITestAidlInterface.Stub() {

        private var position = 0f

        override fun rpm(): Int {
            position += 0.001f
            return MAX_RPM / 2 + (MAX_RPM / 2 * sin(position) + MAX_RPM / 4 * sin(2 * position) + MAX_RPM / 8 * sin(4 * position) + MAX_RPM / 16 * sin(
                8 * position)).toInt()
        }

        override fun speed(): Int {
            position += 0.001f
            return MAX_SPEED / 2 + (MAX_SPEED / 2 * sin(position) + MAX_SPEED / 10 * sin(2 * position) + MAX_SPEED / 20 * sin(4 * position)).toInt()
        }

    }

    override fun onBind(intent: Intent?): IBinder? = binder

    companion object {
        private const val MAX_SPEED = 120
        private const val MAX_RPM = 9000
    }
}