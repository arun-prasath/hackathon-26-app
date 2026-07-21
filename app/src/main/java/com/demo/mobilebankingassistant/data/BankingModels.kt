package com.demo.mobilebankingassistant.data

data class BankingUser(
    val id: String,
    val name: String,
    val clientType: String,
    val lastLogin: String,
    val totalAssets: String,
    val totalLiabilities: String,
    val savingsAccount: Account,
    val investments: String,
    val insurancePolicies: String,
    val transactions: List<Transaction>,
    val debitCards: List<CardInfo>,
    val creditCards: List<CardInfo> = emptyList(),
    val creditCardStatements: List<CreditCardStatement> = emptyList(),
    val bankingProducts: List<BankingProduct> = emptyList()
)

data class Account(
    val name: String,
    val maskedNumber: String,
    val balance: String,
    val currency: String
)

data class Transaction(
    val date: String,
    val title: String,
    val subtitle: String,
    val amount: String,
    val credit: Boolean,
    val category: String = "Other"
)

data class CardInfo(
    val name: String,
    val maskedNumber: String,
    val type: String,
    val status: String = "ACTIVE"
)

data class CreditCardStatement(
    val date: String,
    val merchant: String,
    val description: String,
    val amount: String,
    val category: String
)

data class BankingProduct(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val interestRate: String,
    val minimumAmount: String,
    val tenure: String,
    val eligibility: String,
    val fees: String,
    val keyBenefits: List<String>
)

data class SpendingChartSlice(
    val category: String,
    val amount: Double
)
