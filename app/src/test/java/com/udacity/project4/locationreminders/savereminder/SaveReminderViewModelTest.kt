package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Use a fake data source to be injected into the viewmodel
    private lateinit var fakeDataSource: FakeDataSource

    private val reminder =
        ReminderDataItem("TITLE", "DESC", "LOCATION", 1.0, 1.0, "1")

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUpViewModel() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun saveReminder_addReminderToDataSource() = runBlockingTest {

        //WHEN
        saveReminderViewModel.saveReminder(reminder)
        val checkReminder = fakeDataSource.getReminder("1") as Result.Success

        //THEN
        assertThat(checkReminder.data.id, `is`(reminder.id))
        assertThat(checkReminder.data.title, `is`(reminder.title))
        assertThat(checkReminder.data.description, `is`(reminder.description))
        assertThat(checkReminder.data.location, `is`(reminder.location))
        assertThat(checkReminder.data.latitude, `is`(reminder.latitude))
        assertThat(checkReminder.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveReminder_checkLoading() = mainCoroutineRule.runBlockingTest {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        //WHEN
        saveReminderViewModel.saveReminder(reminder)

        // THEN loading indicator is shown
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        //THEN
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun saveReminder_noTitle_showSnackbarAndReturnFalse() {
        val noReminderTitle = ReminderDataItem("", "DESC", "LOCATION", 1.0, 1.0, "1")

        //WHEN
        saveReminderViewModel.validateAndSaveReminder(noReminderTitle)

        //THEN
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )
    }

    @Test
    fun saveReminder_noLocation_showSnackbarAndReturnFalse() {
        val noReminderLocation =
            ReminderDataItem("TITLE", "DESC", "", 1.0, 1.0, "1")

        //WHEN
        saveReminderViewModel.validateAndSaveReminder(noReminderLocation)

        //THEN
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )
    }

}