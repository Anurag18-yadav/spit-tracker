package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.ExpenseViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Expense Split", appName)
    }

    @Test
    fun `verify expense calculation and obligations`() {
        val viewModel = ExpenseViewModel()
        val state = viewModel.uiState.value

        assertEquals(3, state.expenses.size)
        assertEquals(3, state.flatmates.size)

        val breakdowns = viewModel.getBreakdowns()
        assertEquals(3, breakdowns.size)

        val totalSpend = state.expenses.sumOf { it.amount }
        assertEquals(2500.0, totalSpend, 0.01)

        val obligations = viewModel.getObligations()
        assertTrue("Should have payment obligations", obligations.isNotEmpty())
    }
}
