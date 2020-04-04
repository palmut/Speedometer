package net.palmut.speedometer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class SpeedometerVM : ViewModel() {

    val speed = flow<Int> {
        while(viewModelScope.isActive) {
            emit(nextSpeed())
        }
    }.asLiveData(Dispatchers.Main)

    private suspend fun nextSpeed() = withContext(Dispatchers.IO) {
        (System.currentTimeMillis() % 120).toInt()
    }
}