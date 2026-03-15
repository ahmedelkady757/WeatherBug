package com.example.weatherbug.presentation.favourites.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherbug.data.models.FavouriteWeatherItem
import com.example.weatherbug.data.repo.WeatherRepo
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.hamcrest.CoreMatchers.instanceOf

class FavouritesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: FavouritesViewModel
    private lateinit var fakeRepo: WeatherRepo

    private val testDispatcher = StandardTestDispatcher()
    
    private val favItem = FavouriteWeatherItem(
        id = 1,
        cityName = "London",
        country = "UK",
        lat = 51.5,
        lon = 0.1,
        temp = 15.0,
        icon = "",
        description = "cloudy"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = mockk(relaxed = true)

        coEvery { fakeRepo.getAllFavourites() } returns flowOf(listOf(favItem))

        viewModel = FavouritesViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun requestDeleteOne_oneItem_updatesActiveDialog() = runTest(testDispatcher) {
        // Given (done in setup)

        // When
        viewModel.requestDeleteOne(favItem)
        advanceUntilIdle()

        // Then
        val dialogState = viewModel.activeDialog.value
        assertThat(dialogState, instanceOf(FavouritesDialog.DeleteOne::class.java))
        assertThat((dialogState as FavouritesDialog.DeleteOne).item.cityName, `is`("London"))
    }

    @Test
    fun confirmDeleteOne_requestDeleteOne_deletesItemAndDismissesDialog() = runTest(testDispatcher) {
        // Given
        viewModel.requestDeleteOne(favItem)
        advanceUntilIdle()

        // When
        viewModel.confirmDeleteOne()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { fakeRepo.deleteFavourite(favItem) }
        assertThat(viewModel.activeDialog.value, instanceOf(FavouritesDialog.None::class.java))
    }

    @Test
    fun requestAddFavourite_loadingNavigate_updatesAddingState() = runTest(testDispatcher) {
        // Given
        var navigated = false
        val navigateLambda = { navigated = true }

        // When
        viewModel.requestAddFavourite(navigateLambda)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.isAddingFavourite.value, `is`(true))
        assertThat(navigated, `is`(true))
    }
}
