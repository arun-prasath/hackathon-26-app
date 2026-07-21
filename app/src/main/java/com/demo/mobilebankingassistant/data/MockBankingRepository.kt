package com.demo.mobilebankingassistant.data

import android.os.Handler
import android.os.Looper
import com.demo.mobilebankingassistant.util.AppLogger
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MockBankingRepository(
    private val baseUrl: String = MockServiceConfig.BASE_URL
) {
    fun fetchLoggedInUser(onResult: (BankingUser?, Boolean) -> Unit) {
        AppLogger.i("MockData", "Fetching logged-in user from $baseUrl/api/user/demo")
        Thread {
            val result = runCatching {
                val connection = URL("$baseUrl/api/user/demo").openConnection() as HttpURLConnection
                connection.connectTimeout = 2500
                connection.readTimeout = 2500
                connection.requestMethod = "GET"
                connection.inputStream.bufferedReader().use { it.readText() }
            }.mapCatching { body ->
                parseUser(JSONObject(body))
            }.getOrNull()

            Handler(Looper.getMainLooper()).post {
                AppLogger.i("MockData", "User data loaded. connected=${result != null}")
                onResult(result, result != null)
            }
        }.start()
    }

    private fun parseUser(json: JSONObject): BankingUser {
        val accountJson = json.getJSONObject("savingsAccount")
        val transactionsJson = json.getJSONArray("transactions")
        val cardsJson = json.getJSONArray("debitCards")
        val creditCardsJson = json.optJSONArray("creditCards")
        val creditCardStatementsJson = json.optJSONArray("creditCardStatements")
        val bankingProductsJson = json.optJSONArray("bankingProducts")

        return BankingUser(
            id = json.getString("id"),
            name = json.getString("name"),
            clientType = json.getString("clientType"),
            lastLogin = json.getString("lastLogin"),
            totalAssets = json.getString("totalAssets"),
            totalLiabilities = json.getString("totalLiabilities"),
            savingsAccount = Account(
                name = accountJson.getString("name"),
                maskedNumber = accountJson.getString("maskedNumber"),
                balance = accountJson.getString("balance"),
                currency = accountJson.getString("currency")
            ),
            investments = json.getString("investments"),
            insurancePolicies = json.getString("insurancePolicies"),
            transactions = List(transactionsJson.length()) { index ->
                val item = transactionsJson.getJSONObject(index)
                Transaction(
                    date = item.getString("date"),
                    title = item.getString("title"),
                    subtitle = item.getString("subtitle"),
                    amount = item.getString("amount"),
                    credit = item.getBoolean("credit"),
                    category = item.optString("category", "Other")
                )
            },
            debitCards = List(cardsJson.length()) { index ->
                val item = cardsJson.getJSONObject(index)
                CardInfo(
                    name = item.getString("name"),
                    maskedNumber = item.getString("maskedNumber"),
                    type = item.getString("type"),
                    status = item.optString("status", "ACTIVE")
                )
            },
            creditCards = if (creditCardsJson == null) {
                emptyList()
            } else {
                List(creditCardsJson.length()) { index ->
                    val item = creditCardsJson.getJSONObject(index)
                    CardInfo(
                        name = item.getString("name"),
                        maskedNumber = item.getString("maskedNumber"),
                        type = item.getString("type"),
                        status = item.optString("status", "ACTIVE")
                    )
                }
            },
            creditCardStatements = if (creditCardStatementsJson == null) {
                emptyList()
            } else {
                List(creditCardStatementsJson.length()) { index ->
                    val item = creditCardStatementsJson.getJSONObject(index)
                    CreditCardStatement(
                        date = item.getString("date"),
                        merchant = item.getString("merchant"),
                        description = item.getString("description"),
                        amount = item.getString("amount"),
                        category = item.getString("category")
                    )
                }
            },
            bankingProducts = if (bankingProductsJson == null) {
                emptyList()
            } else {
                List(bankingProductsJson.length()) { index ->
                    val item = bankingProductsJson.getJSONObject(index)
                    val benefitsJson = item.optJSONArray("keyBenefits")
                    BankingProduct(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        category = item.getString("category"),
                        description = item.getString("description"),
                        interestRate = item.optString("interestRate", "Not applicable"),
                        minimumAmount = item.optString("minimumAmount", "Not specified"),
                        tenure = item.optString("tenure", "Not specified"),
                        eligibility = item.optString("eligibility", "Subject to bank approval"),
                        fees = item.optString("fees", "As per schedule of charges"),
                        keyBenefits = if (benefitsJson == null) {
                            emptyList()
                        } else {
                            List(benefitsJson.length()) { benefitIndex -> benefitsJson.getString(benefitIndex) }
                        }
                    )
                }
            }
        )
    }

    fun updateCreditCardStatus(block: Boolean, onResult: (Boolean, BankingUser?) -> Unit) {
        val action = if (block) "block" else "unblock"
        AppLogger.i("MockData", "Updating credit card status action=$action")
        Thread {
            val result = runCatching {
                val connection = URL("$baseUrl/api/cards/credit/$action").openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 10000
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.outputStream.use { it.write("{}".toByteArray()) }
                val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
                val body = stream.bufferedReader().use { it.readText() }
                parseUser(JSONObject(body).getJSONObject("user"))
            }.getOrNull()

            Handler(Looper.getMainLooper()).post {
                AppLogger.i("MockData", "Credit card status update complete. success=${result != null}")
                onResult(result != null, result)
            }
        }.start()
    }
}
