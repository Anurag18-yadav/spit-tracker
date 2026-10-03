package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.model.Expense
import com.example.model.PaymentObligation
import com.example.model.PersonBreakdown
import com.example.model.ScreenDestination
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

data class ExpenseUiState(
    val currentScreen: ScreenDestination = ScreenDestination.HOME,
    val flatmates: List<String> = listOf("Vivek", "Anurag", "Rohan"),
    val expenses: List<Expense> = emptyList(),
    val settledObligationIds: Set<String> = emptySet(),
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val showPrivacyDialog: Boolean = false,
    val showClearDataDialog: Boolean = false,
    val showAddFlatmateDialog: Boolean = false,
    val notificationMessage: String? = null
)

class ExpenseViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadSampleData()
    }

    fun navigateTo(screen: ScreenDestination) {
        _uiState.update { it.copy(currentScreen = screen, notificationMessage = null) }
    }

    fun loadSampleData() {
        val sampleFlatmates = listOf("Vivek", "Anurag", "Rohan")
        val sampleExpenses = listOf(
            Expense(
                id = "exp_1",
                item = "WiFi & Broadband",
                amount = 1200.0,
                paidBy = "Vivek",
                date = "02 Oct",
                category = "Utilities",
                splitAmong = sampleFlatmates
            ),
            Expense(
                id = "exp_2",
                item = "Grocery & Milk",
                amount = 850.0,
                paidBy = "Anurag",
                date = "28 Sep",
                category = "Groceries",
                splitAmong = sampleFlatmates
            ),
            Expense(
                id = "exp_3",
                item = "House Cleaning Supplies",
                amount = 450.0,
                paidBy = "Rohan",
                date = "25 Sep",
                category = "Household",
                splitAmong = sampleFlatmates
            )
        )
        _uiState.update {
            it.copy(
                flatmates = sampleFlatmates,
                expenses = sampleExpenses,
                settledObligationIds = emptySet(),
                notificationMessage = "Sample data loaded"
            )
        }
    }

    fun addExpense(
        item: String,
        amount: Double,
        paidBy: String,
        date: String,
        category: String,
        splitAmong: List<String>
    ): Boolean {
        if (item.isBlank() || amount <= 0.0 || paidBy.isBlank()) {
            return false
        }
        val effectiveSplit = if (splitAmong.isEmpty()) _uiState.value.flatmates else splitAmong
        val newExpense = Expense(
            id = UUID.randomUUID().toString(),
            item = item.trim(),
            amount = amount,
            paidBy = paidBy,
            date = date.ifBlank { currentDateFormatted() },
            category = category.ifBlank { "General" },
            splitAmong = effectiveSplit
        )
        _uiState.update { current ->
            current.copy(
                expenses = listOf(newExpense) + current.expenses,
                notificationMessage = "Added ${newExpense.item} (₹${formatCurrency(newExpense.amount)})"
            )
        }
        return true
    }

    fun deleteExpense(id: String) {
        _uiState.update { current ->
            val expense = current.expenses.find { it.id == id }
            current.copy(
                expenses = current.expenses.filterNot { it.id == id },
                notificationMessage = expense?.let { "Removed ${it.item}" }
            )
        }
    }

    fun toggleSettleObligation(id: String) {
        _uiState.update { current ->
            val isAlreadySettled = current.settledObligationIds.contains(id)
            val updated = if (isAlreadySettled) {
                current.settledObligationIds - id
            } else {
                current.settledObligationIds + id
            }
            current.copy(
                settledObligationIds = updated,
                notificationMessage = if (isAlreadySettled) "Marked as pending" else "Settlement recorded!"
            )
        }
    }

    fun addFlatmate(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isBlank() || _uiState.value.flatmates.any { it.equals(trimmed, ignoreCase = true) }) {
            return false
        }
        _uiState.update {
            it.copy(
                flatmates = it.flatmates + trimmed,
                showAddFlatmateDialog = false,
                notificationMessage = "Added flatmate: $trimmed"
            )
        }
        return true
    }

    fun removeFlatmate(name: String) {
        _uiState.update {
            if (it.flatmates.size <= 2) {
                it.copy(notificationMessage = "Must keep at least 2 flatmates")
            } else {
                it.copy(
                    flatmates = it.flatmates - name,
                    notificationMessage = "Removed $name"
                )
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun clearAllData() {
        _uiState.update {
            it.copy(
                expenses = emptyList(),
                settledObligationIds = emptySet(),
                showClearDataDialog = false,
                notificationMessage = "All expenses cleared"
            )
        }
    }

    fun setShowPrivacyDialog(show: Boolean) {
        _uiState.update { it.copy(showPrivacyDialog = show) }
    }

    fun setShowClearDataDialog(show: Boolean) {
        _uiState.update { it.copy(showClearDataDialog = show) }
    }

    fun setShowAddFlatmateDialog(show: Boolean) {
        _uiState.update { it.copy(showAddFlatmateDialog = show) }
    }

    fun clearNotification() {
        _uiState.update { it.copy(notificationMessage = null) }
    }

    // Calculations
    fun getBreakdowns(): List<PersonBreakdown> {
        val state = _uiState.value
        val flatmates = state.flatmates
        val expenses = state.expenses
        val totalGroupSpend = expenses.sumOf { it.amount }

        return flatmates.map { person ->
            val paid = expenses.filter { it.paidBy == person }.sumOf { it.amount }
            var owes = 0.0
            for (expense in expenses) {
                val beneficiaries = if (expense.splitAmong.isNotEmpty()) expense.splitAmong else flatmates
                if (beneficiaries.contains(person)) {
                    owes += expense.amount / beneficiaries.size
                }
            }
            val balance = paid - owes
            val percentage = if (totalGroupSpend > 0) ((paid / totalGroupSpend) * 100).toFloat() else 0f
            PersonBreakdown(
                name = person,
                paid = paid,
                owes = owes,
                balance = balance,
                spendPercentage = percentage
            )
        }
    }

    fun getObligations(): List<PaymentObligation> {
        val breakdowns = getBreakdowns()
        val debtors = mutableListOf<Pair<String, Double>>()
        val creditors = mutableListOf<Pair<String, Double>>()

        for (person in breakdowns) {
            val net = (person.balance * 100).roundToInt() / 100.0
            if (net < -0.5) {
                debtors.add(person.name to abs(net))
            } else if (net > 0.5) {
                creditors.add(person.name to net)
            }
        }

        debtors.sortByDescending { it.second }
        creditors.sortByDescending { it.second }

        val obligations = mutableListOf<PaymentObligation>()
        var dIdx = 0
        var cIdx = 0

        val dAmounts = debtors.map { it.second }.toMutableList()
        val cAmounts = creditors.map { it.second }.toMutableList()

        while (dIdx < debtors.size && cIdx < creditors.size) {
            val debtorName = debtors[dIdx].first
            val creditorName = creditors[cIdx].first
            val dRemaining = dAmounts[dIdx]
            val cRemaining = cAmounts[cIdx]

            val settledAmount = min(dRemaining, cRemaining)
            if (settledAmount > 0.5) {
                val id = "obl_${debtorName}_${creditorName}_${(settledAmount * 100).toInt()}"
                obligations.add(
                    PaymentObligation(
                        id = id,
                        debtor = debtorName,
                        creditor = creditorName,
                        amount = settledAmount,
                        isSettled = _uiState.value.settledObligationIds.contains(id)
                    )
                )
            }

            dAmounts[dIdx] -= settledAmount
            cAmounts[cIdx] -= settledAmount

            if (dAmounts[dIdx] <= 0.05) dIdx++
            if (cAmounts[cIdx] <= 0.05) cIdx++
        }

        return obligations
    }

    companion object {
        fun currentDateFormatted(): String {
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            return sdf.format(Date())
        }

        fun formatCurrency(amount: Double): String {
            return if (amount % 1.0 == 0.0) {
                "%,d".format(Locale.getDefault(), amount.toLong())
            } else {
                "%,.2f".format(Locale.getDefault(), amount)
            }
        }
    }
}
