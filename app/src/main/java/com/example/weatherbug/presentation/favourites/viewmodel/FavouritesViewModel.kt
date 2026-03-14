package com.example.weatherbug.presentation.favourites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbug.data.models.FavouriteWeatherItem
import com.example.weatherbug.data.repo.WeatherRepo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


sealed class FavouritesDialog {
    data class DeleteOne(val item: FavouriteWeatherItem) : FavouritesDialog()
    data object DeleteAll : FavouritesDialog()
    data object None : FavouritesDialog()
}


class FavouritesViewModel(private val repo: WeatherRepo) : ViewModel() {


    val favourites: StateFlow<List<FavouriteWeatherItem>> = repo
        .getAllFavourites()
        .stateIn(
            scope          = viewModelScope,
            started        = SharingStarted.WhileSubscribed(5_000),
            initialValue   = emptyList()
        )


    private val _activeDialog = MutableStateFlow<FavouritesDialog>(FavouritesDialog.None)
    val activeDialog: StateFlow<FavouritesDialog> = _activeDialog.asStateFlow()

    private val _isAddingFavourite = MutableStateFlow(false)
    val isAddingFavourite: StateFlow<Boolean> = _isAddingFavourite.asStateFlow()


    fun requestDeleteOne(item: FavouriteWeatherItem) {

        _activeDialog.value = FavouritesDialog.DeleteOne(item)
    }

    fun requestDeleteAll() {

        _activeDialog.value = FavouritesDialog.DeleteAll
    }

    fun dismissDialog() {
        _activeDialog.value = FavouritesDialog.None
    }


    fun confirmDeleteOne() {
        val dialog = _activeDialog.value as? FavouritesDialog.DeleteOne ?: return
        viewModelScope.launch {

            repo.deleteFavourite(dialog.item)
            _activeDialog.value = FavouritesDialog.None
        }
    }

    fun confirmDeleteAll() {
        viewModelScope.launch {

            repo.deleteAllFavourites()
            _activeDialog.value = FavouritesDialog.None
        }
    }

    fun requestAddFavourite(onNavigate: () -> Unit) {
        viewModelScope.launch {
            _isAddingFavourite.value = true
            kotlinx.coroutines.delay(100)
            onNavigate()
        }
    }
    
    fun resetAddingState() {
        _isAddingFavourite.value = false
    }
}