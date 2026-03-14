package com.example.weatherbug.presentation.splash.viewmodel

import androidx.lifecycle.ViewModel
import com.example.weatherbug.data.datasource.local.IAppDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


sealed class SplashNavEvent {
    data object Idle           : SplashNavEvent()
    data object NavigateToHome : SplashNavEvent()
}


class SplashViewModel() : ViewModel() {

    private val _navEvent = MutableStateFlow<SplashNavEvent>(SplashNavEvent.Idle)
    val navEvent: StateFlow<SplashNavEvent> = _navEvent.asStateFlow()

    fun onAnimationCompleted() {
        _navEvent.value = SplashNavEvent.NavigateToHome
    }
}