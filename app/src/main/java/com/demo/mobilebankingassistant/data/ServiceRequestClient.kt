package com.demo.mobilebankingassistant.data

import com.demo.mobilebankingassistant.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AddressChangeRequest(
    val line1: String,
    val city: String,
    val state: String,
    val postalCode: String
)

data class ServiceRequestResult(
    val serviceRequestNumber: String?,
    val message: String,
    val success: Boolean
)

class ServiceRequestClient(
    private val baseUrl: String = MockServiceConfig.BASE_URL
) {
    suspend fun submitAddressChange(
        userId: String,
        request: AddressChangeRequest
    ): ServiceRequestResult = withContext(Dispatchers.IO) {
        AppLogger.i("ServiceRequest", "Submitting address change request for user=$userId, baseUrl=$baseUrl")
        runCatching {
            val payload = JSONObject()
                .put("userId", userId)
                .put("address", JSONObject()
                    .put("line1", request.line1)
                    .put("city", request.city)
                    .put("state", request.state)
                    .put("postalCode", request.postalCode)
                )
                .toString()

            val connection = URL("$baseUrl/api/service-requests/address").openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 20000
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.outputStream.use { it.write(payload.toByteArray()) }

            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader().use { it.readText() }
            AppLogger.i("ServiceRequest", "Address change response status=${connection.responseCode}, bodyLength=${body.length}")
            val json = JSONObject(body)

            ServiceRequestResult(
                serviceRequestNumber = json.optString("serviceRequestNumber").ifBlank { null },
                message = json.optString("message", "Address change request submitted."),
                success = connection.responseCode in 200..299
            )
        }.getOrElse {
            AppLogger.e("ServiceRequest", "Address change request failed", it)
            ServiceRequestResult(
                serviceRequestNumber = null,
                message = "Address change service is unavailable at $baseUrl. Start mock-service or update MockServiceConfig.BASE_URL.",
                success = false
            )
        }
    }
}
