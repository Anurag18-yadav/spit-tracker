package com.example.model

data class Expense(
    val id: String,
    val item: String,
    val amount: Double,
    val paidBy: String,
    val date: String,
    val category: String = "General",
    val splitAmong: List<String> = emptyList()
)

data class Flatmate(
    val id: String,
    val name: String,
    val colorHex: String = "#6366F1"
)

data class PaymentObligation(
    val id: String,
    val debtor: String, // Who owes
    val creditor: String, // To whom
    val amount: Double,
    val isSettled: Boolean = false
)

data class PersonBreakdown(
    val name: String,
    val paid: Double,
    val owes: Double,
    val balance: Double, // paid - owes (positive = to receive, negative = owes)
    val spendPercentage: Float
)

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class ScreenDestination(val title: String) {
    HOME("Home"),
    ADD_EXPENSE("Add Expense"),
    SETTLEMENT("Settlement"),
    BREAKDOWN("Breakdown"),
    SETTINGS("Settings")
}
