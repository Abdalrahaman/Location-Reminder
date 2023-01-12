package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel

    // Use a fake data source to be injected into the viewmodel
    private lateinit var fakeDataSource: FakeDataSource

    private val reminder1 = ReminderDTO("TITLE1", "DESC1", "LOCATION1", 1.0, 1.0, "1")
    private val reminder2 = ReminderDTO("TITLE2", "DESC2", "LOCATION2", 2.0, 2.0, "2")
    private val reminder3 = ReminderDTO("TITLE3", "DESC3", "LOCATION3", 3.0, 3.0, "3")
    private val reminder4 = ReminderDTO("TITLE4", "DESC4", "LOCATION4", 4.0, 4.0, "4")

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupRemindersListViewModel() {
        fakeDataSource = FakeDataSource()

        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun clearDataSource() = runBlockingTest {
        fakeDataSource.deleteAllReminders()
    }

    @Test
    fun loadReminders_loadsFourReminders() = runBlockingTest {

        fakeDataSource.saveReminder(reminder1)
        fakeDataSource.saveReminder(reminder2)
        fakeDataSource.saveReminder(reminder3)
        fakeDataSource.saveReminder(reminder4)

        //WHEN
        remindersListViewModel.loadReminders()

        //THEN
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is`(4))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun loadReminders_checkLoading() = mainCoroutineRule.runBlockingTest {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        fakeDataSource.saveReminder(reminder1)
        //when
        remindersListViewModel.loadReminders()

        // Then loading indicator is shown
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Then loading indicator is hidden
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun loadReminders_empty_returnsShowNoDataIsTrue() = runBlockingTest {
        //WHEN
        remindersListViewModel.loadReminders()
        //THEN
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is`(0))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))

    }

    @Test
    fun loadReminders_error_callSnackBarErrorToDisplay() = runBlockingTest {
        //when
        fakeDataSource.setReturnError(true)
        remindersListViewModel.loadReminders()

        //then
        assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(),
            `is`("Error Occurred when get all reminders")
        )
    }

}