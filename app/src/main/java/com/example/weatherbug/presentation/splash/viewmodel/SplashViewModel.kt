package com.example.weatherbug.presentation.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbug.data.datasource.local.IAppDataStore
import com.example.weatherbug.util.AppLogger
import com.example.weatherbug.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


sealed class SplashNavEvent {
    data object Idle                : SplashNavEvent()
    data object NavigateToHome      : SplashNavEvent()
}



class SplashViewModel(
    private val appDataStore: IAppDataStore
) : ViewModel() {

    private val _navEvent = MutableStateFlow<SplashNavEvent>(SplashNavEvent.Idle)
    val navEvent: StateFlow<SplashNavEvent> = _navEvent.asStateFlow()


}