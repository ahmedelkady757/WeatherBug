package com.example.weatherbug.presentation.home.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherbug.core.util.Constants
import com.example.weatherbug.core.util.ResponseState
import com.example.weatherbug.data.datasource.local.IAppDataStore
import com.example.weatherbug.data.models.*
import com.example.weatherbug.data.repo.WeatherRepo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
import kotlinx.coroutines.test.advanceUntilIdle

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeRepo: WeatherRepo
    private lateinit var fakeDataStore: IAppDataStore

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = mockk()
        fakeDataStore = mockk()

        // Mock datastore flows
        coEvery { fakeDataStore.savedLatFlow } returns flowOf(30.0)
        coEvery { fakeDataStore.savedLonFlow } returns flowOf(31.0)
        coEvery { fakeDataStore.tempUnitFlow } returns flowOf(Constants.UNIT_METRIC)
        coEvery { fakeDataStore.effectiveLangFlow } returns flowOf(Constants.LANG_ENGLISH)
        coEvery { fakeDataStore.windUnitFlow } returns flowOf(Constants.WIND_UNIT_MS)

        val weatherResponse = WeatherResponse(
            coord = CoordData(31.0, 30.0),
            weather = listOf(WeatherResponse.WeatherCondition(800, "Clear", "clear sky", "01d")),
            base = "stations",
            main = WeatherResponse.MainData(25.0, 25.0, 25.0, 25.0, 1010, 50, 1010, 1010),
            visibility = 10000,
            wind = WeatherResponse.WindData(5.0, 100, 10.0),
            clouds = WeatherResponse.CloudsData(0),
            dt = 1600000000,
            sys = WeatherResponse.SysData("EG", 1600000000L, 1600040000L),
            timezone = 7200,
            id = 360630,
            name = "Cairo",
            cod = 200
        )
        
        val hourlyResponse = HourlyForecastResponse(
            cod = "200",
            message = 0,
            cnt = 1,
            list = emptyList(),
            city = HourlyForecastResponse.CityData(360630, "Cairo", CoordData(31.0, 30.0), "EG", 0, 7200, 0, 0)
        )
        
        val dailyResponse = DailyForecastResponse(
            city = DailyForecastResponse.CityData(360630, "Cairo", CoordData(31.0, 30.0), "EG", 0, 7200, 0, 0),
            cod = "200",
            message = 0.0,
            cnt = 1,
            list = emptyList()
        )

        coEvery { 
            fakeRepo.getCurrentWeather(30.0, 31.0, Constants.UNIT_METRIC, Constants.LANG_ENGLISH) 
        } returns ResponseState.Success(weatherResponse)
        
        coEvery { 
            fakeRepo.getHourlyForecast(30.0, 31.0, Constants.HOURLY_COUNT, Constants.UNIT_METRIC, Constants.LANG_ENGLISH) 
        } returns ResponseState.Success(hourlyResponse)
        
        coEvery { 
            fakeRepo.getDailyForecast(30.0, 31.0, Constants.DAILY_COUNT, Constants.UNIT_METRIC, Constants.LANG_ENGLISH) 
        } returns ResponseState.Success(dailyResponse)

        viewModel = HomeViewModel(fakeRepo, fakeDataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun homeViewModel_init_loadsWeatherSuccessfully() = runTest(testDispatcher) {
        // Given is done in setUp

        // When
        advanceUntilIdle()

        // Then
        val currentState = viewModel.currentWeatherState.value
        assertThat(currentState is ResponseState.Success, `is`(true))
        val currentData = (currentState as ResponseState.Success).data
        assertThat(currentData.name, `is`("Cairo"))
    }

    @Test
    fun homeViewModel_retry_reloadsWeather() = runTest(testDispatcher) {
        // Given
        advanceUntilIdle() // let init finish

        // When
        viewModel.retry()
        advanceUntilIdle()

        // Then
        val hourlyState = viewModel.hourlyState.value
        assertThat(hourlyState is ResponseState.Success, `is`(true))
    }

    @Test
    fun homeViewModel_errorInRepo_showsFailureState() = runTest(testDispatcher) {
        // Given
        coEvery { 
            fakeRepo.getCurrentWeather(any(), any(), any(), any()) 
        } returns ResponseState.Failure("Network Error")
        
        // Re-init viewmodel to trigger error state
        viewModel = HomeViewModel(fakeRepo, fakeDataStore)

        // When
        advanceUntilIdle()

        // Then
        val currentState = viewModel.currentWeatherState.value
        assertThat(currentState is ResponseState.Failure, `is`(true))
        val errorMessage = (currentState as ResponseState.Failure).errorMessage
        assertThat(errorMessage, `is`("Network Error"))
    }
}
