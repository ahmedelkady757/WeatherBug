package com.example.weatherbug.data.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.weatherbug.data.models.FavouriteWeatherItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class WeatherBugDaoTest {



    private lateinit var database: WeatherBugDatabase
    private lateinit var dao: WeatherBugDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherBugDatabase::class.java
        ).build()
        dao = database.weatherBugDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertFavourite_oneFavouriteItem_savesToDatabase() = runTest {
        // Given
        val item = FavouriteWeatherItem(
            id = 1, cityName = "Alex", country = "EG", lat = 31.0, lon = 29.0, temp = 20.0, icon = "", description = "Sunny"
        )

        // When
        dao.insertFavourite(item)

        // Then
        val allFavs = dao.getAllFavourites().first()
        assertThat(allFavs.size, `is`(1))
        assertThat(allFavs[0].cityName, `is`("Alex"))
    }

    @Test
    fun deleteFavourite_oneFavouriteItem_removesFromDatabase() = runTest {
        // Given
        val item = FavouriteWeatherItem(
            id = 2, cityName = "Cairo", country = "EG", lat = 30.0, lon = 31.0, temp = 30.0, icon = "", description = "Hot"
        )
        dao.insertFavourite(item)

        // When
        dao.deleteFavourite(item)

        // Then
        val allFavs = dao.getAllFavourites().first()
        assertThat(allFavs.isEmpty(), `is`(true))
    }

    @Test
    fun deleteAllFavourites_twoFavouriteItem_clearsTable() = runTest {
        // Given
        val item1 = FavouriteWeatherItem(id = 1, cityName = "A", country = "EG", lat = 0.0, lon = 0.0, temp = 0.0, icon = "", description = "")
        val item2 = FavouriteWeatherItem(id = 2, cityName = "B", country = "EG", lat = 0.0, lon = 0.0, temp = 0.0, icon = "", description = "")
        dao.insertFavourite(item1)
        dao.insertFavourite(item2)

        // When
        dao.deleteAllFavourites()

        // Then
        val allFavs = dao.getAllFavourites().first()
        assertThat(allFavs.isEmpty(), `is`(true))
    }
}
