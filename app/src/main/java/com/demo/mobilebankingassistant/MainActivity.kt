package com.demo.mobilebankingassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.demo.mobilebankingassistant.data.AssistantChatMessage
import com.demo.mobilebankingassistant.ui.theme.ScBlue
import com.demo.mobilebankingassistant.data.ChatRole
import com.demo.mobilebankingassistant.data.BankingUser
import com.demo.mobilebankingassistant.data.MockBankingRepository
import com.demo.mobilebankingassistant.data.ServiceRequestClient
import com.demo.mobilebankingassistant.ui.AccountDetailsScreen
import com.demo.mobilebankingassistant.ui.AddressChangeScreen
import com.demo.mobilebankingassistant.ui.HomeScreen
import com.demo.mobilebankingassistant.ui.LoginScreen
import com.demo.mobilebankingassistant.ui.LogoutScreen
import com.demo.mobilebankingassistant.ui.AssistantDialog
import com.demo.mobilebankingassistant.ui.BiometricLoginScreen
import com.demo.mobilebankingassistant.ui.CardServicesScreen
import com.demo.mobilebankingassistant.ui.LogoutConfirmDialog
import com.demo.mobilebankingassistant.ui.PinCardSelectScreen
import com.demo.mobilebankingassistant.ui.PinChangeFormScreen
import com.demo.mobilebankingassistant.ui.ServicesScreen
import com.demo.mobilebankingassistant.ui.StatementDetailsScreen
import com.demo.mobilebankingassistant.ui.theme.MobileBankingAssistantTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobileBankingAssistantTheme {
                val navController = rememberNavController()
                var showAssistant by remember { mutableStateOf(false) }
                var showLogoutConfirm by remember { mutableStateOf(false) }
                var user by remember { mutableStateOf<BankingUser?>(null) }
                var isLoadingUser by remember { mutableStateOf(true) }
                var loadError by remember { mutableStateOf<String?>(null) }
                var serviceConnected by remember { mutableStateOf(false) }
                var assistantMessages by remember { mutableStateOf<List<AssistantChatMessage>>(emptyList()) }
                val repository = remember { MockBankingRepository() }
                val serviceRequestClient = remember { ServiceRequestClient() }
                val coroutineScope = rememberCoroutineScope()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                fun loadUserFromMockService() {
                    isLoadingUser = true
                    loadError = null
                    repository.fetchLoggedInUser { loadedUser, connected ->
                        user = loadedUser
                        serviceConnected = connected
                        isLoadingUser = false
                        loadError = if (loadedUser == null) {
                            "Could not load logged-in user from mock service. Start mock-service or update the service URL for your phone."
                        } else {
                            null
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    loadUserFromMockService()
                }

                fun initialAssistantMessages(loadedUser: BankingUser): List<AssistantChatMessage> {
                    return listOf(
                            AssistantChatMessage(
                                role = ChatRole.ASSISTANT,
                                text = "Hi ${loadedUser.name}. How can I help you today?",
                                routeLabel = "Local cache - session ready"
                            )
                    )
                }

                LaunchedEffect(user?.id) {
                    val loadedUser = user
                    if (loadedUser != null && assistantMessages.isEmpty()) {
                        assistantMessages = initialAssistantMessages(loadedUser)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        fun goHome() {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }

                        NavHost(navController = navController, startDestination = "login") {
                            composable("login") {
                                val loadedUser = user
                                if (loadedUser == null) {
                                    MockServiceStateScreen(
                                        isLoading = isLoadingUser,
                                        error = loadError,
                                        onRetry = { loadUserFromMockService() }
                                    )
                                } else {
                                    LoginScreen(
                                        user = loadedUser,
                                        onLoginSuccess = {
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        },
                                        onBiometricClick = { navController.navigate("biometrics") }
                                    )
                                }
                            }
                            composable("biometrics") {
                                BiometricLoginScreen(onCancel = { navController.popBackStack() })
                            }
                            composable("home") {
                                val loadedUser = user ?: return@composable MockServiceStateScreen(isLoadingUser, loadError) { loadUserFromMockService() }
                                HomeScreen(
                                    user = loadedUser,
                                    serviceConnected = serviceConnected,
                                    onLogout = { showLogoutConfirm = true },
                                    onAssistantClick = { showAssistant = true },
                                    onHomeClick = { goHome() },
                                    onDepositsClick = { navController.navigate("accountDetails") },
                                    onStatementClick = { navController.navigate("statementDetails") },
                                    onServicesClick = { navController.navigate("services") },
                                    onPinChangeClick = { navController.navigate("pinSelect") }
                                )
                            }
                            composable("accountDetails") {
                                val loadedUser = user ?: return@composable MockServiceStateScreen(isLoadingUser, loadError) { loadUserFromMockService() }
                                AccountDetailsScreen(
                                    user = loadedUser,
                                    onBack = { navController.popBackStack() },
                                    onLogout = { showLogoutConfirm = true },
                                    onHomeClick = { goHome() },
                                    onStatementClick = { navController.navigate("statementDetails") },
                                    onServicesClick = { navController.navigate("services") }
                                )
                            }
                            composable("statementDetails") {
                                val loadedUser = user ?: return@composable MockServiceStateScreen(isLoadingUser, loadError) { loadUserFromMockService() }
                                StatementDetailsScreen(user = loadedUser, onBack = { navController.popBackStack() })
                            }
                            composable("services") {
                                val loadedUser = user ?: return@composable MockServiceStateScreen(isLoadingUser, loadError) { loadUserFromMockService() }
                                ServicesScreen(
                                    user = loadedUser,
                                    onBack = { navController.popBackStack() },
                                    onLogout = { showLogoutConfirm = true },
                                    onHomeClick = { goHome() },
                                    onCardsClick = { navController.navigate("cardServices") },
                                    onPinChangeClick = { navController.navigate("pinSelect") },
                                    onAddressChangeClick = { navController.navigate("addressChange") }
                                )
                            }
                            composable("addressChange") {
                                val loadedUser = user ?: return@composable MockServiceStateScreen(isLoadingUser, loadError) { loadUserFromMockService() }
                                AddressChangeScreen(
                                    user = loadedUser,
                                    onBack = { navController.popBackStack() },
                                    onSubmit = { request ->
                                        coroutineScope.launch {
                                            val result = serviceRequestClient.submitAddressChange(loadedUser.id, request)
                                            assistantMessages = assistantMessages + AssistantChatMessage(
                                                role = ChatRole.ASSISTANT,
                                                text = if (result.success) {
                                                    "Recent event: Address change request submitted. Service request number ${result.serviceRequestNumber}."
                                                } else {
                                                    result.message
                                                },
                                                routeLabel = if (result.success) "Backend service - recent event" else "Backend service failed"
                                            )
                                            showAssistant = true
                                            goHome()
                                        }
                                    }
                                )
                            }
                            composable("cardServices") {
                                val loadedUser = user ?: return@composable MockServiceStateScreen(isLoadingUser, loadError) { loadUserFromMockService() }
                                CardServicesScreen(
                                    user = loadedUser,
                                    onBack = { navController.popBackStack() },
                                    onPinChangeClick = { navController.navigate("pinSelect") }
                                )
                            }
                            composable("pinSelect") {
                                val loadedUser = user ?: return@composable MockServiceStateScreen(isLoadingUser, loadError) { loadUserFromMockService() }
                                PinCardSelectScreen(
                                    cards = loadedUser.debitCards,
                                    onBack = { navController.popBackStack() },
                                    onNext = { navController.navigate("pinForm") }
                                )
                            }
                            composable("pinForm") {
                                val loadedUser = user ?: return@composable MockServiceStateScreen(isLoadingUser, loadError) { loadUserFromMockService() }
                                PinChangeFormScreen(
                                    card = loadedUser.debitCards.first(),
                                    onBack = { navController.popBackStack() },
                                    onPinChanged = {
                                        assistantMessages = assistantMessages + AssistantChatMessage(
                                            role = ChatRole.ASSISTANT,
                                            text = "Recent event: Debit/ATM Card PIN changed successfully for ${loadedUser.debitCards.first().maskedNumber}.",
                                            routeLabel = "App workflow - recent event"
                                        )
                                        showAssistant = true
                                        goHome()
                                    }
                                )
                            }
                            composable("logout") {
                                LogoutScreen(onLoginClick = {
                                    navController.navigate("login") {
                                        popUpTo("logout") { inclusive = true }
                                    }
                                })
                            }
                        }

                        if (currentRoute in setOf("accountDetails", "statementDetails", "services", "cardServices", "pinSelect", "pinForm", "addressChange") && !showAssistant) {
                            FloatingActionButton(
                                onClick = { showAssistant = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 20.dp, bottom = 92.dp)
                                    .size(58.dp),
                                containerColor = ScBlue,
                                contentColor = Color.White
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Assistant")
                            }
                        }

                        if (showAssistant) {
                            user?.let {
                                AssistantDialog(
                                    user = it,
                                    messages = assistantMessages,
                                    onMessagesChange = { updatedMessages -> assistantMessages = updatedMessages },
                                    onChangePinRequested = { navController.navigate("pinSelect") },
                                    onChangeAddressRequested = { navController.navigate("addressChange") },
                                    onCreditCardStatusChangeRequested = { block ->
                                        repository.updateCreditCardStatus(block) { success, updatedUser ->
                                            if (updatedUser != null) {
                                                user = updatedUser
                                                serviceConnected = true
                                            }
                                            val card = updatedUser?.creditCards?.firstOrNull() ?: user?.creditCards?.firstOrNull()
                                            assistantMessages = assistantMessages + AssistantChatMessage(
                                                role = ChatRole.ASSISTANT,
                                                text = if (success) {
                                                    val status = if (block) "blocked" else "active"
                                                    "Recent event: ${card?.name ?: "Credit card"} ${card?.maskedNumber.orEmpty()} is now $status."
                                                } else {
                                                    "I could not update the credit card status. Please check that the mock service is running."
                                                },
                                                routeLabel = if (success) "Backend service - recent event" else "Backend service failed"
                                            )
                                        }
                                    },
                                    onNewConversation = { assistantMessages = initialAssistantMessages(it) },
                                    onDismiss = { showAssistant = false }
                                )
                            }
                        }

                        if (showLogoutConfirm) {
                            LogoutConfirmDialog(
                                onConfirm = {
                                    showLogoutConfirm = false
                                    navController.navigate("logout") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                onStay = { showLogoutConfirm = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MockServiceStateScreen(
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = ScBlue)
                Spacer(Modifier.height(16.dp))
                Text("Loading user from mock service...")
            } else {
                Text(error ?: "Mock service unavailable.", color = Color(0xFF2D2935))
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}
