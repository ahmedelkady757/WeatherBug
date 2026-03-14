package com.example.weatherbug.presentation.settings.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherbug.core.util.Constants
import com.example.weatherbug.data.datasource.local.IAppDataStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var fakeDataStore: IAppDataStore

    private val testDispatcher = StandardTestDispatcher()
    private var isGpsRefreshed = false

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDataStore = mockk(relaxed = true)
        
        coEvery { fakeDataStore.themeFlow } returns flowOf(Constants.THEME_LIGHT)
        coEvery { fakeDataStore.languageFlow } returns flowOf(Constants.LANG_ENGLISH)
        coEvery { fakeDataStore.tempUnitFlow } returns flowOf(Constants.UNIT_METRIC)
        coEvery { fakeDataStore.windUnitFlow } returns flowOf(Constants.WIND_UNIT_MS)
        coEvery { fakeDataStore.locationModeFlow } returns flowOf(Constants.LOCATION_GPS)

        isGpsRefreshed = false
        val mockOnRefreshGps : () -> Unit = { isGpsRefreshed = true }

        viewModel = SettingsViewModel(fakeDataStore, mockOnRefreshGps)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun settingsViewModel_setTheme_savesToDataStore() = runTest(testDispatcher) {
        // Given
        val newTheme = Constants.THEME_DARK

        // When
        viewModel.setTheme(newTheme)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { fakeDataStore.saveTheme(newTheme) }
    }

    @Test
    fun settingsViewModel_setLocationModeMap_emitsNavigateEvent() = runTest(testDispatcher) {
        // Given
        val newMode = Constants.LOCATION_MAP
        val events = mutableListOf<SettingsNavEvent>()
        
        // When
        val job = launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navEvent.collect { events.add(it) }
        }
        viewModel.setLocationMode(newMode)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { fakeDataStore.saveLocationMode(newMode) }
        assertThat(events.isNotEmpty(), `is`(true))
        assertThat(events[0], `is`(SettingsNavEvent.NavigateToMapPicker))
        
        job.cancel()
    }

    @Test
    fun settingsViewModel_setLocationModeGps_refreshesGps() = runTest(testDispatcher) {
        // Given
        val newMode = Constants.LOCATION_GPS
        
        // When
        viewModel.setLocationMode(newMode)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { fakeDataStore.saveLocationMode(newMode) }
        assertThat(isGpsRefreshed, `is`(true))
    }
}
