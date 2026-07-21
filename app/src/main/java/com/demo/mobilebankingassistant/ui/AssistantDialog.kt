package com.demo.mobilebankingassistant.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.demo.mobilebankingassistant.data.AssistantChatMessage
import com.demo.mobilebankingassistant.data.BankingUser
import com.demo.mobilebankingassistant.data.ChatAction
import com.demo.mobilebankingassistant.data.ChatRole
import com.demo.mobilebankingassistant.data.RemoteAiClient
import com.demo.mobilebankingassistant.data.SpendingChartSlice
import com.demo.mobilebankingassistant.llm.LocalLlmClient
import com.demo.mobilebankingassistant.logic.RouteType
import com.demo.mobilebankingassistant.logic.SmartIntentRouter
import com.demo.mobilebankingassistant.ui.theme.ScBlue
import com.demo.mobilebankingassistant.ui.theme.ScGreen
import com.demo.mobilebankingassistant.util.AppLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantDialog(
    user: BankingUser,
    messages: List<AssistantChatMessage>,
    onMessagesChange: (List<AssistantChatMessage>) -> Unit,
    onChangePinRequested: () -> Unit,
    onChangeAddressRequested: () -> Unit,
    onCreditCardStatusChangeRequested: (Boolean) -> Unit,
    onNewConversation: () -> Unit,
    onDismiss: () -> Unit
) {
    val router = remember { SmartIntentRouter() }
    val context = LocalContext.current
    val localLlmClient = remember { LocalLlmClient(context.applicationContext) }
    val remoteAiClient = remember { RemoteAiClient() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var llmStatus by remember { mutableStateOf(localLlmClient.status()) }
    var input by remember { mutableStateOf("") }
    var isRouting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var importMessage by remember { mutableStateOf<String?>(null) }

    val modelPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            AppLogger.i("Assistant", "Model picker returned uri=$uri")
            isImporting = true
            importMessage = "Importing selected model..."
            coroutineScope.launch {
                val imported = localLlmClient.importModel(uri)
                llmStatus = localLlmClient.status()
                importMessage = if (imported) {
                    "Model imported into private app storage."
                } else {
                    "Model import failed. Select a valid .litertlm file."
                }
                isImporting = false
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF8F6FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .imePadding()
        ) {
            ChatHeader(
                statusText = when {
                    llmStatus.initialized -> "Local model initialized"
                    llmStatus.available -> "Local model found"
                    else -> "Rules-only fallback"
                },
                modelPath = llmStatus.modelPath,
                onImport = {
                    AppLogger.i("Assistant", "Import Local Model clicked")
                    modelPicker.launch(arrayOf("application/octet-stream", "*/*"))
                },
                onNewConversation = onNewConversation,
                onDismiss = onDismiss,
                importing = isImporting
            )

            importMessage?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    color = if (llmStatus.available) ScGreen else Color(0xFF6B6570)
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(
                        message = message,
                        onAction = { action ->
                            when (action) {
                                ChatAction.CHANGE_PIN -> {
                                    onDismiss()
                                    onChangePinRequested()
                                }
                                ChatAction.CHANGE_ADDRESS -> {
                                    onDismiss()
                                    onChangeAddressRequested()
                                }
                                ChatAction.BLOCK_CREDIT_CARD -> onCreditCardStatusChangeRequested(true)
                                ChatAction.UNBLOCK_CREDIT_CARD -> onCreditCardStatusChangeRequested(false)
                            }
                        }
                    )
                }

                if (isRouting) {
                    item {
                        ThinkingBubble()
                    }
                }
            }

            ChatInputRow(
                value = input,
                enabled = !isRouting && !isImporting,
                onValueChange = { input = it },
                onSend = {
                    val userText = input.trim()
                    if (userText.isNotBlank() && !isRouting) {
                        input = ""
                        val updatedMessages = messages + AssistantChatMessage(ChatRole.USER, userText)
                        onMessagesChange(updatedMessages)
                        isRouting = true

                        coroutineScope.launch {
                            AppLogger.i("Assistant", "Send clicked. queryLength=${userText.length}, historySize=${messages.size}, modelAvailable=${llmStatus.available}")
                            val historySnapshot = updatedMessages
                            val basicMessage = basicLocalResponse(userText, user)
                            val statementAnalytics = isStatementAnalyticsQuery(userText)
                            if (statementAnalytics) {
                                AppLogger.i("Assistant", "Statement analytics query detected. Sending statement context to local LLM for answer generation.")
                            }
                            val actionDecision = if (basicMessage == null && !statementAnalytics) {
                                localLlmClient.classifyAssistantAction(userText, user, historySnapshot)
                            } else {
                                AppLogger.i("Assistant", "Skipping action classification. basic=${basicMessage != null}, statementAnalytics=$statementAnalytics")
                                null
                            }
                            AppLogger.i("Assistant", "Local action decision=${actionDecision?.action}, confidence=${actionDecision?.confidence}, reason=${actionDecision?.reason}")
                            val trustedAction = actionDecision
                                ?.takeIf { shouldTrustWorkflowAction(userText, it.action, it.confidence) }
                                ?.action
                            if (actionDecision != null && trustedAction == null) {
                                AppLogger.i("Assistant", "Ignoring untrusted action=${actionDecision.action}, confidence=${actionDecision.confidence}, query=$userText")
                            }
                            val fallbackAction = if (statementAnalytics) null else fallbackWorkflowAction(userText)
                            if (fallbackAction != null && fallbackAction != trustedAction) {
                                AppLogger.i("Assistant", "Using guarded fallback action=$fallbackAction for query=$userText")
                            }
                            val workflowAction = trustedAction ?: fallbackAction
                            val workflowMessage = if (statementAnalytics) {
                                null
                            } else {
                                assistantWorkflowResponse(workflowAction, user, userText)
                            }
                            val llmRoute = if (workflowMessage == null && basicMessage == null) {
                                localLlmClient.classifyRoute(userText, user, historySnapshot)
                            } else {
                                null
                            }

                            val assistantMessage = if (workflowMessage != null || basicMessage != null) {
                                workflowMessage ?: basicMessage!!
                            } else {
                                val decision = router.route(userText, llmRoute)
                                llmStatus = localLlmClient.status()
                                val rawLocalAnswer = decision.localAnswer
                                val answer = if (decision.route == RouteType.LOCAL) {
                                    rawLocalAnswer?.takeIf { it.isNotBlank() } ?: decision.reasoning
                                } else {
                                    AppLogger.i("Assistant", "Route is BACKEND. Calling RemoteAiClient with chat history.")
                                    val remote = remoteAiClient.ask(userText, user, historySnapshot)
                                    remote.answer
                                }
                                val finalAnswer = if (
                                    decision.route == RouteType.LOCAL &&
                                    statementAnalytics &&
                                    isInvalidLlmAnswer(rawLocalAnswer.orEmpty())
                                ) {
                                    AppLogger.i("Assistant", "Replacing invalid placeholder LLM answer with cache analytics fallback.")
                                    buildCreditCardStatementFallbackAnswer(user)
                                } else {
                                    answer
                                }

                                AssistantChatMessage(
                                    role = ChatRole.ASSISTANT,
                                    text = finalAnswer,
                                    routeLabel = if (decision.route == RouteType.LOCAL) {
                                        "Local LLM + cache - ${decision.tokenSavings} tokens saved"
                                    } else {
                                        "AI Factory - backend API"
                                    },
                                    chart = if (decision.route == RouteType.LOCAL && shouldShowStatementChart(userText)) {
                                        buildCreditCardSpendingChart(user)
                                    } else {
                                        emptyList()
                                    }
                                )
                            }

                            onMessagesChange(updatedMessages + assistantMessage)
                            isRouting = false
                        }
                    }
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose { localLlmClient.close() }
    }
}

@Composable
private fun ChatHeader(
    statusText: String,
    modelPath: String,
    importing: Boolean,
    onImport: () -> Unit,
    onNewConversation: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(42.dp), color = ScBlue, shape = CircleShape) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = "Assistant agent", tint = Color.White, modifier = Modifier.size(25.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("SC Smart Assistant", color = Color(0xFF1F1A27), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Smart Banking with AI", color = Color(0xFF6B6570), fontSize = 12.sp)
            }
            IconButton(onClick = onNewConversation) {
                Icon(Icons.Default.Add, contentDescription = "New conversation", tint = Color(0xFF2D2935))
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF2D2935))
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AssistChip(
                onClick = { },
                label = { Text(statusText, fontSize = 11.sp) },
                colors = AssistChipDefaults.assistChipColors(labelColor = ScGreen)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onImport,
                enabled = !importing,
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                if (importing) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = ScBlue)
                }
                Spacer(Modifier.width(6.dp))
                Text(if (importing) "Importing" else "Import", color = ScBlue, fontSize = 12.sp)
            }
        }
        Text("Model path: $modelPath", color = Color(0xFF6B6570), fontSize = 10.sp, lineHeight = 12.sp)
    }
}

@Composable
private fun ChatBubble(message: AssistantChatMessage, onAction: (ChatAction) -> Unit = {}) {
    val isUser = message.role == ChatRole.USER
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Surface(
                modifier = Modifier.widthIn(max = 300.dp),
                color = if (isUser) ScBlue else Color.White,
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                shadowElevation = if (isUser) 0.dp else 1.dp
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (isUser) Color.White else Color(0xFF2D2935),
                    fontSize = 14.sp,
                    lineHeight = 19.sp
                )
            }
            if (!isUser && message.chart.isNotEmpty()) {
                SpendingChart(
                    slices = message.chart,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .widthIn(max = 300.dp)
                )
            }
            if (!isUser && !message.routeLabel.isNullOrBlank()) {
                SourceBadge(message.routeLabel)
            }
            message.action?.let { action ->
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { onAction(action) },
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (action) {
                            ChatAction.CHANGE_PIN -> "Go to PIN Change"
                        ChatAction.CHANGE_ADDRESS -> "Open Address Form"
                        ChatAction.BLOCK_CREDIT_CARD -> "Block Credit Card"
                        ChatAction.UNBLOCK_CREDIT_CARD -> "Unblock Credit Card"
                    },
                        color = ScBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ThinkingBubble() {
    var dotCount by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(420)
            dotCount = if (dotCount == 6) 1 else dotCount + 1
        }
    }

    ChatBubble(
        AssistantChatMessage(
            role = ChatRole.ASSISTANT,
            text = "Thinking${".".repeat(dotCount)}"
        )
    )
}

@Composable
private fun SourceBadge(label: String) {
    val isBackend = label.contains("backend", ignoreCase = true) ||
        label.contains("AI Factory", ignoreCase = true)
    Surface(
        modifier = Modifier.padding(top = 4.dp, start = 8.dp),
        color = if (isBackend) Color(0xFFFFF3E2) else Color(0xFFE9FFF2),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isBackend) Color(0xFFFFC781) else Color(0xFFBDEECD))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            color = if (isBackend) Color(0xFF8A4B00) else Color(0xFF057A3C),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SpendingChart(slices: List<SpendingChartSlice>, modifier: Modifier = Modifier) {
    val maxAmount = slices.maxOfOrNull { it.amount } ?: 1.0
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFE2DEE8))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Credit card spend by category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2935))
            Spacer(Modifier.height(8.dp))
            slices.take(8).forEach { slice ->
                val fraction = (slice.amount / maxAmount).toFloat().coerceIn(0.05f, 1f)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(slice.category, modifier = Modifier.width(92.dp), fontSize = 10.sp, color = Color(0xFF4F4856))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .background(Color(0xFFE8EEF7), RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(12.dp)
                                .background(ScBlue, RoundedCornerShape(8.dp))
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(formatInr(slice.amount), fontSize = 10.sp, color = Color(0xFF2D2935))
                }
            }
        }
    }
}

private fun shouldTrustWorkflowAction(query: String, action: String, confidence: Double): Boolean {
    if (confidence < 0.65) return false
    if (basicGreeting(query)) return false
    return when (action) {
        "BALANCE" -> hasAny(query, "balance", "assets", "deposit", "liabilit", "investment", "insurance")
        "SPENDING_SUMMARY" -> hasAny(query, "spending", "spend", "expense", "summary", "chart", "categor")
        "LAST_TRANSACTIONS" -> hasAny(query, "transaction", "statement", "recent", "last")
        "CHANGE_PIN" -> hasAny(query, "pin") && hasAny(query, "change", "set", "reset", "hange")
        "CHANGE_ADDRESS" -> hasAny(query, "address") && hasAny(query, "change", "update", "submit", "hange")
        "BLOCK_CREDIT_CARD" -> hasAny(query, "credit card", "card") && hasAny(query, "block", "freeze", "disable")
        "UNBLOCK_CREDIT_CARD" -> hasAny(query, "credit card", "card") && hasAny(query, "unblock", "unfreeze", "reactivate", "active")
        else -> false
    }
}

private fun basicLocalResponse(query: String, user: BankingUser): AssistantChatMessage? {
    val normalized = query.trim().lowercase()
    return when {
        basicGreeting(normalized) -> AssistantChatMessage(
            role = ChatRole.ASSISTANT,
            text = "Hi ${user.name}. How can I help you today?",
            routeLabel = localCacheLabel(query)
        )
        else -> null
    }
}

private fun fallbackWorkflowAction(query: String): String? {
    val lower = query.lowercase()
    return when {
        basicGreeting(lower) -> null
        hasAny(lower, "unblock", "unfreeze", "reactivate") && hasAny(lower, "credit card", "card") -> "UNBLOCK_CREDIT_CARD"
        hasAny(lower, "block", "freeze", "disable") && hasAny(lower, "credit card", "card") -> "BLOCK_CREDIT_CARD"
        hasAny(lower, "pin") && hasAny(lower, "change", "set", "reset", "hange") -> "CHANGE_PIN"
        hasAny(lower, "address") && hasAny(lower, "change", "update", "submit", "hange") -> "CHANGE_ADDRESS"
        hasAny(lower, "spending", "spend", "expense", "chart", "categor") -> "SPENDING_SUMMARY"
        hasAny(lower, "transaction", "statement", "recent", "last") -> "LAST_TRANSACTIONS"
        hasAny(lower, "balance", "assets", "deposit", "liabilit", "investment", "insurance") -> "BALANCE"
        else -> null
    }
}

private fun isStatementAnalyticsQuery(query: String): Boolean {
    val lower = query.lowercase()
    val asksForStatementAnalysis = hasAny(
        lower,
        "credit card statement",
        "statement",
        "spending",
        "spend",
        "expense",
        "transaction",
        "transactions",
        "merchant",
        "category",
        "grocer",
        "fuel",
        "dining",
        "shopping",
        "subscription",
        "travel",
        "compare",
        "month"
    )
    return asksForStatementAnalysis && !hasAny(lower, "pin", "address", "block", "unblock")
}

private fun hasAny(query: String, vararg terms: String): Boolean {
    val lower = query.lowercase()
    return terms.any { lower.contains(it) }
}

private fun localCacheLabel(query: String): String {
    val tokensSaved = (query.length / 4).coerceAtLeast(3) + 20
    return "Local cache - $tokensSaved tokens saved"
}

private fun basicGreeting(query: String): Boolean {
    return when (query.trim().lowercase()) {
        "hi", "hello", "hey", "good morning", "good afternoon", "good evening" -> true
        else -> false
    }
}

private fun assistantWorkflowResponse(action: String?, user: BankingUser, query: String): AssistantChatMessage? {
    return when (action) {
        "BALANCE" -> {
            AssistantChatMessage(
                role = ChatRole.ASSISTANT,
                text = buildBalanceAnswer(user),
                routeLabel = localCacheLabel("balance")
            )
        }
        "SPENDING_SUMMARY" -> null
        "LAST_TRANSACTIONS" -> {
            AssistantChatMessage(
                role = ChatRole.ASSISTANT,
                text = user.transactions.take(5).joinToString(prefix = "Your last transactions:\n") {
                    "${it.date}: ${it.title} - ${it.amount} ${if (it.credit) "credit" else "debit"}"
                },
                routeLabel = "Local LLM + account cache - 55 tokens saved"
            )
        }
        "CHANGE_PIN" -> {
            AssistantChatMessage(
                role = ChatRole.ASSISTANT,
                text = "I can take you to the Debit/ATM Card PIN Change flow. For security, the PIN itself is changed only inside the secure app screen, not inside chat.",
                routeLabel = "Local LLM action + app workflow - 45 tokens saved",
                action = ChatAction.CHANGE_PIN
            )
        }
        "CHANGE_ADDRESS" -> {
            AssistantChatMessage(
                role = ChatRole.ASSISTANT,
                text = "I can open the address change form. Once submitted, the backend will create a service request number and I will add it to this chat as a recent event.",
                routeLabel = "Local LLM action + backend workflow",
                action = ChatAction.CHANGE_ADDRESS
            )
        }
        "BLOCK_CREDIT_CARD" -> {
            val card = user.creditCards.firstOrNull()
            AssistantChatMessage(
                role = ChatRole.ASSISTANT,
                text = if (card?.status == "BLOCKED") {
                    "Your ${card.name} ${card.maskedNumber} is already blocked."
                } else {
                    "I can block your ${card?.name ?: "credit card"} ${card?.maskedNumber.orEmpty()} now. This will update the card status in the backend mock service."
                },
                routeLabel = "Local LLM action + backend workflow",
                action = if (card?.status == "BLOCKED") null else ChatAction.BLOCK_CREDIT_CARD
            )
        }
        "UNBLOCK_CREDIT_CARD" -> {
            val card = user.creditCards.firstOrNull()
            AssistantChatMessage(
                role = ChatRole.ASSISTANT,
                text = if (card?.status == "ACTIVE") {
                    "Your ${card.name} ${card.maskedNumber} is already active."
                } else {
                    "I can unblock your ${card?.name ?: "credit card"} ${card?.maskedNumber.orEmpty()} now. This will update the card status in the backend mock service."
                },
                routeLabel = "Local LLM action + backend workflow",
                action = if (card?.status == "ACTIVE") null else ChatAction.UNBLOCK_CREDIT_CARD
            )
        }
        else -> null
    }
}

private fun shouldShowStatementChart(query: String): Boolean {
    val lower = query.lowercase()
    return hasAny(lower, "chart", "visual", "graph", "bar chart", "category-wise", "category wise")
}

private fun isInvalidLlmAnswer(answer: String): Boolean {
    val lower = answer.lowercase()
    return listOf(
        "inr x",
        "inr a",
        "inr b",
        "inr c",
        "current month inr x",
        "please provide an answer",
        "customer service",
        "faq section",
        "accessed by contacting"
    ).any { lower.contains(it) }
}

private fun buildCreditCardStatementFallbackAnswer(user: BankingUser): String {
    val chart = buildCreditCardSpendingChart(user)
    val total = chart.sumOf { it.amount }
    val topCategory = chart.firstOrNull()
    return buildString {
        append("Based on your cached credit-card statements, total spend is ${formatInr(total)} across ${user.creditCardStatements.size} transactions. ")
        if (topCategory != null) {
            append("The highest category is ${topCategory.category} at ${formatInr(topCategory.amount)}. ")
        }
        append("Suggestions: review high-value categories first and set a monthly alert for shopping, groceries, and fuel.")
    }
}

private fun buildBalanceAnswer(user: BankingUser): String {
    val creditCard = user.creditCards.firstOrNull()
    return buildString {
        append("Your current savings balance is ${user.savingsAccount.balance}. ")
        append("Total assets are ${user.totalAssets}, investments are ${user.investments}, and total liabilities are ${user.totalLiabilities}.")
        if (creditCard != null) {
            append(" Your ${creditCard.name} ${creditCard.maskedNumber} is ${creditCard.status.lowercase()}.")
        }
    }
}

private fun buildCreditCardSpendingChart(user: BankingUser): List<SpendingChartSlice> {
    return user.creditCardStatements
        .groupBy { it.category }
        .map { (category, items) ->
            SpendingChartSlice(category, items.sumOf { parseAmount(it.amount) })
        }
        .sortedByDescending { it.amount }
}

private fun formatInr(amount: Double): String = "INR %,.0f".format(amount)

private fun parseAmount(amount: String): Double {
    return amount.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
}

@Composable
private fun ChatInputRow(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 6.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about your account...") },
                shape = RoundedCornerShape(24.dp),
                minLines = 1,
                maxLines = 4
            )
            Spacer(Modifier.width(8.dp))
            FloatingActionButton(
                onClick = onSend,
                modifier = Modifier.size(50.dp),
                containerColor = ScBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                if (enabled) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                } else {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                }
            }
        }
    }
}
