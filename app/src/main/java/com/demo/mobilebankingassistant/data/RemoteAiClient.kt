package com.demo.mobilebankingassistant.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.demo.mobilebankingassistant.util.AppLogger
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class RemoteAiResponse(
    val answer: String,
    val provider: String,
    val model: String,
    val error: String? = null
)

class RemoteAiClient(
    private val baseUrl: String = MockServiceConfig.BASE_URL
) {
    suspend fun ask(
        query: String,
        user: BankingUser,
        history: List<AssistantChatMessage>
    ): RemoteAiResponse = withContext(Dispatchers.IO) {
        AppLogger.i("RemoteAI", "Backend AI request started. baseUrl=$baseUrl, queryLength=${query.length}")
        runCatching {
            val messages = org.json.JSONArray()
            history.takeLast(10).forEach {
                messages.put(
                    JSONObject()
                        .put("role", it.role.name.lowercase())
                        .put("text", it.text)
                )
            }
            val context = JSONObject()
                .put("name", user.name)
                .put("clientType", user.clientType)
                .put("totalAssets", user.totalAssets)
                .put("totalLiabilities", user.totalLiabilities)
                .put("savingsAccountName", user.savingsAccount.name)
                .put("savingsAccountMaskedNumber", user.savingsAccount.maskedNumber)
                .put("balance", user.savingsAccount.balance)
                .put("investments", user.investments)
                .put("insurancePolicies", user.insurancePolicies)
                .put("creditCardName", user.creditCards.firstOrNull()?.name ?: "")
                .put("creditCardMaskedNumber", user.creditCards.firstOrNull()?.maskedNumber ?: "")
                .put("creditCardStatus", user.creditCards.firstOrNull()?.status ?: "UNKNOWN")
            if (requiresStatementContext(query)) {
                context
                    .put("accountTransactions", org.json.JSONArray().apply {
                        user.transactions.forEach {
                            put(
                                JSONObject()
                                    .put("date", it.date)
                                    .put("title", it.title)
                                    .put("amount", it.amount)
                                    .put("credit", it.credit)
                                    .put("category", it.category)
                            )
                        }
                    })
                    .put("creditCardStatements", org.json.JSONArray().apply {
                        user.creditCardStatements.forEach {
                            put(
                                JSONObject()
                                    .put("date", it.date)
                                    .put("merchant", it.merchant)
                                    .put("description", it.description)
                                    .put("amount", it.amount)
                                    .put("category", it.category)
                            )
                        }
                    })
            }
            if (requiresProductContext(query)) {
                context.put("bankingProducts", org.json.JSONArray().apply {
                    user.bankingProducts.forEach {
                        put(
                            JSONObject()
                                .put("id", it.id)
                                .put("name", it.name)
                                .put("category", it.category)
                                .put("description", it.description)
                                .put("interestRate", it.interestRate)
                                .put("minimumAmount", it.minimumAmount)
                                .put("tenure", it.tenure)
                                .put("eligibility", it.eligibility)
                                .put("fees", it.fees)
                                .put("keyBenefits", org.json.JSONArray(it.keyBenefits))
                        )
                    }
                })
            }
            val payload = JSONObject()
                .put("query", query)
                .put("customerContext", context)
                .put("messages", messages)
                .toString()

            val connection = URL("$baseUrl/api/ai/chat").openConnection() as HttpURLConnection
            connection.connectTimeout = 20000
            connection.readTimeout = 60000
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.outputStream.use { it.write(payload.toByteArray()) }

            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val body = stream.bufferedReader().use { it.readText() }
            AppLogger.i("RemoteAI", "Backend AI response received. httpStatus=${connection.responseCode}, bodyLength=${body.length}")
            AppLogger.d("RemoteAI", "Backend AI raw response=${body.take(1000)}")
            val json = JSONObject(body)

            RemoteAiResponse(
                answer = json.optString("answer", json.optString("error", "No answer returned")),
                provider = json.optString("provider", "mock-service"),
                model = json.optString("model", "unknown"),
                error = json.optString("error").ifBlank { null }
            )
        }.getOrElse {
            AppLogger.e("RemoteAI", "Backend AI request failed", it)
            RemoteAiResponse(
                answer = "Backend AI service is unreachable. Start mock-service with OPENAI_API_KEY, or update the base URL for a physical phone.",
                provider = "mock-service",
                model = "unavailable",
                error = it.message
            )
        }
    }

    private fun requiresStatementContext(query: String): Boolean {
        val lower = query.lowercase()
        return listOf("statement", "transaction", "spending", "spend", "summary", "chart", "categor").any { lower.contains(it) }
    }

    private fun requiresProductContext(query: String): Boolean {
        val lower = query.lowercase()
        return listOf(
            "product",
            "offer",
            "mutual fund",
            "investment",
            "invest",
            "fixed deposit",
            "fd",
            "deposit rate",
            "home loan",
            "personal loan",
            "car loan",
            "loan",
            "interest rate",
            "tenure",
            "eligibility"
        ).any { lower.contains(it) }
    }
}
