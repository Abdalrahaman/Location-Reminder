package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    private val reminder1 = ReminderDTO("TITLE1", "DESC1", "LOCATION1", 1.0, 1.0, "1")
    private val reminder2 = ReminderDTO("TITLE2", "DESC2", "LOCATION2", 2.0, 2.0, "2")
    private val reminder3 = ReminderDTO("TITLE3", "DESC3", "LOCATION3", 3.0, 3.0, "3")

    // Executes each reminder synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_retrievesReminderById() = runBlocking {
        // GIVEN - a new reminder saved in the database
        localRepository.saveReminder(reminder1)

        // WHEN  - Reminder retrieved by ID
        val result = localRepository.getReminder(reminder1.id)

        // THEN - Same reminder is returned
        result as Result.Success
        Assert.assertThat(result.data.id, `is`(reminder1.id))
        Assert.assertThat(result.data.title, `is`(reminder1.title))
        Assert.assertThat(result.data.description, `is`(reminder1.description))
        Assert.assertThat(result.data.location, `is`(reminder1.location))
        Assert.assertThat(result.data.latitude, `is`(reminder1.latitude))
        Assert.assertThat(result.data.longitude, `is`(reminder1.longitude))
    }

    @Test
    fun saveReminders_retrievesAllReminders() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)
        localRepository.saveReminder(reminder3)

        // WHEN  - All reminders retrieved.
        val result = localRepository.getReminders()

        // THEN - Correct number of reminders returned.
        result as Result.Success
        assertThat(result.data.size, `is`(3))
    }

    @Test
    fun saveReminders_deleteAllReminders() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)
        localRepository.saveReminder(reminder3)

        // WHEN  - All reminders deleted.
        localRepository.deleteAllReminders()

        val result = localRepository.getReminders()
        // THEN - empty reminders returned.
        result as Result.Success
        assertThat(result.data.size, `is`(0))

    }
}