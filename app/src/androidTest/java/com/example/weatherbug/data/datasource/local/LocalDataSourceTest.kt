package com.example.weatherbug.data.datasource.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.weatherbug.data.db.WeatherBugDatabase
import com.example.weatherbug.data.models.FavouriteWeatherItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class LocalDataSourceTest {


    private lateinit var database: WeatherBugDatabase
    private lateinit var localDataSource: LocalDataSource

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherBugDatabase::class.java
        ).allowMainThreadQueries().build()
        localDataSource = LocalDataSource(database.weatherBugDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertFavourite_oneFavouriteItem_savesToDatabase() = runTest {
        // Given
        val item = FavouriteWeatherItem(
            id = 10, cityName = "Luxor", country = "EG", lat = 25.0, lon = 32.0, temp = 40.0, icon = "", description = "Hot"
        )

        // When
        localDataSource.insertFavourite(item)

        // Then
        val allFavs = localDataSource.getAllFavourites().first()
        assertThat(allFavs.size, `is`(1))
        assertThat(allFavs[0].cityName, `is`("Luxor"))
    }

    @Test
    fun deleteFavourite_oneFavouriteItem_removesFromDatabase() = runTest {
        // Given
        val item = FavouriteWeatherItem(
            id = 11, cityName = "Aswan", country = "EG", lat = 24.0, lon = 32.0, temp = 42.0, icon = "", description = "Very Hot"
        )
        localDataSource.insertFavourite(item)

        // When
        localDataSource.deleteFavourite(item)

        // Then
        val allFavs = localDataSource.getAllFavourites().first()
        assertThat(allFavs.isEmpty(), `is`(true))
    }

    @Test
    fun deleteAllFavourites_twoFavouriteItem_clearsTable() = runTest {
        // Given
        val item1 = FavouriteWeatherItem(12, "City1", "C1", 0.0, 0.0, 0.0, "", "")
        val item2 = FavouriteWeatherItem(13, "City2", "C2", 0.0, 0.0, 0.0, "", "")
        localDataSource.insertFavourite(item1)
        localDataSource.insertFavourite(item2)

        // When
        localDataSource.deleteAllFavourites()

        // Then
        val allFavs = localDataSource.getAllFavourites().first()
        assertThat(allFavs.isEmpty(), `is`(true))
    }
}
