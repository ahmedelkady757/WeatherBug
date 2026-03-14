package com.example.weatherbug.data.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherbug.core.util.ResponseState
import com.example.weatherbug.data.datasource.local.ILocalDataSource
import com.example.weatherbug.data.datasource.remote.IRemoteDataSource
import com.example.weatherbug.data.models.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class WeatherRepoImplTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repo: WeatherRepoImpl
    private lateinit var fakeRemote: IRemoteDataSource
    private lateinit var fakeLocal: ILocalDataSource

    @Before
    fun setUp() {
        fakeRemote = mockk()
        fakeLocal = mockk(relaxed = true)
        repo = WeatherRepoImpl(fakeRemote, fakeLocal)
    }

    @Test
    fun weatherRepoImpl_getCurrentWeather_returnsSuccess() = runTest {
        // Given
        val response = WeatherResponse(
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
        coEvery { fakeRemote.getCurrentWeather(any(), any(), any(), any()) } returns response

        // When
        val result = repo.getCurrentWeather(30.0, 31.0, "metric", "en")

        // Then
        assertThat(result is ResponseState.Success, `is`(true))
        assertThat((result as ResponseState.Success).data.name, `is`("Cairo"))
    }

    @Test
    fun weatherRepoImpl_getCurrentWeather_returnsFailureOnError() = runTest {
        // Given
        coEvery { fakeRemote.getCurrentWeather(any(), any(), any(), any()) } throws Exception("Network Error")

        // When
        val result = repo.getCurrentWeather(30.0, 31.0, "metric", "en")

        // Then
        assertThat(result is ResponseState.Failure, `is`(true))
        assertThat((result as ResponseState.Failure).errorMessage, `is`("Network Error"))
    }

    @Test
    fun weatherRepoImpl_getCityByCoordinates_returnsSuccess() = runTest {
        // Given
        val item = GeocodingItem(
            name = "Cairo",
            lat = 30.0,
            lon = 31.0,
            country = "EG",
            localNames = mapOf("en" to "Cairo")
        )
        coEvery { fakeRemote.getCityByCoordinates(any(), any()) } returns listOf(item)

        // When
        val result = repo.getCityByCoordinates(30.0, 31.0)

        // Then
        assertThat(result is ResponseState.Success, `is`(true))
        assertThat((result as ResponseState.Success).data.first().name, `is`("Cairo"))
    }
}
