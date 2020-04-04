package net.palmut.speedometer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class SpeedometerVM : ViewModel() {

    private var current: Float = 0f
    private var increment: Float = 0.1f

    val speed = flow<Float> {
        while(viewModelScope.isActive) {
            emit(nextSpeed())
        }
    }.asLiveData(Dispatchers.Main)

    private suspend fun nextSpeed() = withContext(Dispatchers.IO) {
        current += increment
        if (current > 120f) increment = -.1f
        if (current < 0) increment = .1f
        current
    }
}